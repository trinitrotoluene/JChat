package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.ConfigurationBuilder;
import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.logging.DefaultLogger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public class ChatServer {
    private IConfiguration _config;
    private ILogger _logger;
    private ServerSocket _socket;

    private final Semaphore _cleanupLock;
    private final ConcurrentMessageBus _messageBus;

    private final int BACKLOG = 5;

    public ChatServer(IConfiguration config, ILogger logger) {
        _config = config;
        _logger = logger;
        _cleanupLock = new Semaphore(1);
        _messageBus = new ConcurrentMessageBus();
    }

    public void listen() {
        var serverPort = _config.getString("csp", "14001");
        var serverHost = _config.getString("csa", "127.0.0.1");
        _logger.info("binding on %s:%s", serverHost, serverPort);

        try {
            var port = Integer.parseInt(serverPort);
            var addr = Inet4Address.getByName(serverHost);

            _socket = new ServerSocket(port, BACKLOG, addr);
            _logger.debug("socket bound");

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
        var acceptTask = acceptClients();
        var consoleCommandTask = acceptCommands();

        try {
            CompletableFuture.anyOf(acceptTask, consoleCommandTask).get();
            acceptTask.cancel(true);
            consoleCommandTask.cancel(true);
        }
        catch (InterruptedException | ExecutionException thrownException) {
            _logger.error("An exception was thrown into the server thread: %s", thrownException.getMessage());
        }

        dispose();
    }

    private CompletableFuture<Void> acceptCommands() {
        return CompletableFuture.runAsync(() -> {
            var scanner = new Scanner(System.in);
            while(!Thread.interrupted() && _socket.isBound()) {
                if (!scanner.hasNext()) {
                    try {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                }
                var command = scanner.nextLine();
                _logger.info("executed command %s", command);
            }
        });
    }

    private CompletableFuture<Void> acceptClients() {
        return CompletableFuture.runAsync(() -> {
            _logger.info("ready");
            while(!Thread.interrupted() && _socket.isBound()) {
                try {
                    var clientSocket = _socket.accept();
                    var handshaker = new ClientSessionNegotiator(_logger, clientSocket, _messageBus);
                    CompletableFuture.supplyAsync(handshaker).thenAcceptAsync(this::handleSession);
                }
                catch (IOException ignored) {
                    return;
                }
            }
        });
    }

    private void handleSession(ClientSession session) {
        if (session == null) {
            return;
        }

        try {
            if (!_messageBus.tryAddClient(session)) {
                _logger.debug("%s: already has an existing session", session.getName());
                session.close();
                return;
            }
            _logger.info("%s: connected", session.getName());
            session.run();
            _messageBus.removeClient(session);
            session.close();
            _logger.info("%s: disconnected", session.getName());
        }
        catch (IOException ignored) {
        }
    }

    private void dispose() {
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

    public static void main(String[] args) {
        var display = new ConsoleWriter();
        var logger = new DefaultLogger(ChatServer.class.getSimpleName(), Level.FINE, display);

        var configuration = new ConfigurationBuilder()
                .addConsole(args)
                .build();

        var server = new ChatServer(configuration, logger);

        server.listen();
    }
}
