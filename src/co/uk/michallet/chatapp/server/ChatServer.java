package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.AppThreadPool;
import co.uk.michallet.chatapp.common.ConfigurationBuilder;
import co.uk.michallet.chatapp.common.ConsoleWriter;
import co.uk.michallet.chatapp.common.HelpMenuBuilder;
import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.commands.CommandNotFoundResult;
import co.uk.michallet.chatapp.common.commands.CommandService;
import co.uk.michallet.chatapp.common.logging.DefaultLogger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

/**
 * Main class containing the server entrypoint and connection logic.
 */
public class ChatServer {
    private IConfiguration _config;
    private ILogger _logger;
    // The CommandService handles mapping and executing commands from the console
    private CommandService<ServerCommandContext> _commands;
    private ServerSocket _socket;

    // Semaphore is used to ensure that the serversocket isn't cleaned up multiple times
    private final Semaphore _cleanupLock;
    // CMB implementation stores and broadcasts to connected clients
    private final ConcurrentMessageBus _messageBus;

    // The number of pending connections we'll allow on the socket
    private final int BACKLOG = 5;

    /**
     * Create a new configured instance of a ChatServer
     * @param config The configuration to pull values from
     * @param logger The ILogger implementation to use for logging
     */
    public ChatServer(IConfiguration config, ILogger logger) {
        _config = config;
        _logger = logger;
        _cleanupLock = new Semaphore(1);
        _messageBus = new ConcurrentMessageBus();

        _commands = new CommandService<>();
        _commands.registerCommands(ServerCommands.class);
    }

    /**
     * Shuts down the server.
     */
    public void abort() {
        dispose();
        for (var client : _messageBus.getNames()) {
            try {
                var session = _messageBus.getClient(client);
                session.send(ChatEventFactory.fromGoodbye());
                session.close();
            }
            catch (IOException ignored) {
            }
        }
    }

    /**
     * Bind and listen for client connections on the socket. This method will block.
     */
    public void listen() {
        // Fetch our configuration, otherwise use the specified defaults
        var serverPort = _config.getString("csp", "14001");
        var serverHost = _config.getString("csa", "127.0.0.1");
        _logger.info("binding on %s:%s", serverHost, serverPort);

        try {
            var port = Integer.parseInt(serverPort);
            var addr = Inet4Address.getByName(serverHost);
            // Bind to the provided address
            _socket = new ServerSocket(port, BACKLOG, addr);
            _logger.debug("socket bound");

            // When the application is killed, e.g. ^C then run cleanup of the socket.
            Runtime.getRuntime().addShutdownHook(new Thread(this::dispose));
        }
        catch (UnknownHostException hostEx) {
            _logger.error("invalid host");
            return;
        }
        catch (IOException ioEx) {
            _logger.error("failed to bind the socket: %s", ioEx.getMessage());
            return;
        }

        _logger.info("starting listeners");
        // Fire off an asynchronous call to accept new clients.
        var acceptTask = acceptClients();
        // Fire off another async call to read and execute commands from the console.
        var consoleCommandTask = acceptCommands();

        try {
            // Wrap both calls into a single future and block on any of them completing
            CompletableFuture.anyOf(acceptTask, consoleCommandTask).get();
            // Cancel whichever one may still be running
            acceptTask.cancel(true);
            consoleCommandTask.cancel(true);
        }
        catch (InterruptedException | ExecutionException thrownException) {
            _logger.error("An exception was thrown into the server thread: %s", thrownException.getMessage());
        }
        // Cleanup
        dispose();
    }

    /**
     * Asynchronously accepts new client sessions on the socket.
     * @return Returns a Future that will complete only when cancelled or the socket terminates.
     */
    private CompletableFuture<Void> acceptCommands() {
        // Run this on the default ForkJoinPool
        return CompletableFuture.runAsync(() -> {
            // Scan in input from stdin
            var scanner = new Scanner(System.in);
            while(!Thread.interrupted() && _socket.isBound()) {
                // We don't want to be blocking on nextLine(), spinning on sleeps ensures we observe IRQs
                while(!scanner.hasNextLine()) {
                    try {
                        Thread.sleep(10);
                    }
                    // If we got interrupted, complete the future.
                    catch (InterruptedException e) {
                        return;
                    }
                }
                // Break the command into multi-word tokens
                var command = scanner.nextLine().split(" ");

                if (command.length > 0) {
                    try {
                        // Create a context for our module to be instantiated with
                        var context = new ServerCommandContext(this, _config, _logger, _messageBus);
                        // Try to fetch and execute a command on this context
                        var result = _commands.execute(context, command[0], Arrays.copyOfRange(command, 1, command.length)).get();
                        if (result instanceof CommandNotFoundResult) {
                            _logger.warn("no such command exists");
                            continue;
                        }

                        if (result.isSuccess()) {
                            _logger.debug("executed command %s", command[0]);
                        }
                        else {
                            _logger.warn("%s: %s", command[0], result.getReason());
                        }
                    } catch (ExecutionException | InterruptedException cmdEx) {
                        _logger.error("an exception was thrown executing command %s", command[0]);
                    }
                }
            }
        }, AppThreadPool.getInstance());
    }

    /**
     * Asynchronously accept new client sessions on the socket
     * @return Returns a Future that will complete only when cancelled or the socket terminates.
     */
    private CompletableFuture<Void> acceptClients() {
        // Run this on a threadpool thread
        return CompletableFuture.runAsync(() -> {
            _logger.info("ready");
            while(!Thread.interrupted() && !_socket.isClosed()) {
                try {
                    // Block on accept();
                    var clientSocket = _socket.accept();
                    // Prepare to handle the client handshake
                    var handshaker = new ClientSessionNegotiator(_logger, clientSocket, _messageBus);
                    // Chain an async continuation that calls handleSession() to the result of the handshake
                    CompletableFuture.supplyAsync(handshaker, AppThreadPool.getInstance()).thenAcceptAsync(this::handleSession, AppThreadPool.getInstance());
                }
                catch (IOException ignored) {
                    return;
                }
            }
        }, AppThreadPool.getInstance());
    }

    /**
     * Called asynchronously when a client has completed the handshake process and established a new potential session.
     * @param session The potential session
     */
    private void handleSession(ClientSession session) {
        // If the session wasn't established i.e. failed the handshake, we're done.
        if (session == null) {
            return;
        }

        try {
            // If there is already a user with this name connected, reject the connection
            if (!_messageBus.tryAddClient(session)) {
                _logger.debug("%s: already has an existing session", session.getName());
                session.close();
                return;
            }
            // If they were added to the bus successfully, broadcast their join event.
            _logger.info("%s: connected", session.getName());
            var joinEvent = ChatEventFactory.fromUserJoin(session.getName());
            _messageBus.broadcast(joinEvent);
            // Block
            session.run();
            // Once this method returns, it means they disconnected- time to clean up.
            _messageBus.removeClient(session);
            session.close();
            // Announce the disconnect
            _messageBus.broadcast(ChatEventFactory.fromUserLeave(session.getName()));
            _logger.info("%s: disconnected", session.getName());
        }
        catch (IOException ignored) {
        }
    }

    private void dispose() {
        // If the semaphore is empty, we've already disposed.
        if (!_cleanupLock.tryAcquire()) {
            return;
        }
        try {
            _logger.info("closing socket");
            _socket.close();
            _logger.info("closed");
        }
        catch (IOException ioEx) {
            _logger.error("error closing socket: %s", ioEx.getMessage());
        }
    }

    /**
     * Our entry point.
     */
    public static void main(String[] args) {
        // Create a default scrolling IOutputWriter, we don't have the same UI needs as the client.
        var display = new ConsoleWriter();
        // Configure our logger.
        var logger = new DefaultLogger(ChatServer.class.getSimpleName(), Level.FINE, display);

        // Read in our configuration from the console.
        var configuration = new ConfigurationBuilder()
                .addConsole(args)
                .build();

        var helpMenu = new HelpMenuBuilder()
                .setTitle("==ChatServer==")
                .addItem("csp", "The port that the server should bind to. Defaults to 14001")
                .addItem("csa", "The ipv4 host that the server should bind on. Defaults to 127.0.0.1")
                .build();

        if (configuration.isSet("help")) {
            display.write(helpMenu);
            return;
        }

        // Configure a new ChatServer.
        var server = new ChatServer(configuration, logger);

        // Bind and listen.
        server.listen();
    }
}
