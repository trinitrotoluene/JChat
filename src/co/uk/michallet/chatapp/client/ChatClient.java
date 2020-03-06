package co.uk.michallet.chatapp.client;

import co.uk.michallet.chatapp.common.ConfigurationBuilder;
import co.uk.michallet.chatapp.common.ConsoleWriter;
import co.uk.michallet.chatapp.common.HelpMenuBuilder;
import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.IOutputWriter;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.SDK.GenericClient;
import co.uk.michallet.chatapp.common.commands.CommandNotFoundResult;
import co.uk.michallet.chatapp.common.commands.CommandService;
import co.uk.michallet.chatapp.common.logging.DefaultLogger;
import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.DmEventArgs;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserJoinEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserLeftEventArgs;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * Console based client implementation
 */
public class ChatClient {
    private IConfiguration _config;
    private ILogger _logger;
    private IOutputWriter _displayOutput;
    private GenericClient _client;
    private CommandService<ClientCommandContext> _commands;
    private CompletableFuture<Void> _listenTask;

    private final Scanner _input = new Scanner(System.in);

    // Default value for backoff
    private int _backoff = 2;
    // Exponent to use for exponential backoff
    private final int BACKOFF_FACTOR = 2;

    public ChatClient(IConfiguration config, ILogger logger, IOutputWriter displayOutput) {
        _config = config;
        _logger = logger;
        _displayOutput = displayOutput;
        _client = new GenericClient(logger);
        _commands = new CommandService<>();
        _commands.registerCommands(ClientCommands.class);
    }

    public synchronized void cancel() {
        if (_listenTask != null) {
            _client.dispose();
            _listenTask.cancel(true);
        }
    }

    public void sendEvent(ChatEvent event) {
        _client.sendEvent(event);
    }

    public void connect() throws InterruptedException {
        var serverHost = _config.getString("cca", "127.0.0.1");
        var serverPort = _config.getString("ccp", "14001");

        int port;
        InetAddress host;
        try {
            port = Integer.parseInt(serverPort);
            host = Inet4Address.getByName(serverHost);
        }
        catch (UnknownHostException e) {
            return;
        }

        // Set the supplier and consumer methods of our GenericClient
        hookClient();

        // Loop
        for (;;) {
            // Attempt a connection
            try {
                _logger.info("connecting to %s:%s", host, port);
                _client.connect(host, port);
                break;
            }
            catch (IOException e) {
                _logger.warn("connection failed, retrying in %s seconds", _backoff);
                Thread.sleep(_backoff * 1000);
                _backoff = (int) Math.pow(_backoff, BACKOFF_FACTOR);
            }
        }

        try {
            _displayOutput.clearAll();
            _logger.info("connected!");
            // Start up the client listener task
            _listenTask = _client.runAsync();
            // Block on it so the calling thread does not exit and kill the application
            _listenTask.get();
        }
        catch (ExecutionException e) {
            _logger.error("the server connection was aborted");
        }
        // Clean up
        _client.dispose();
    }

    private void hookClient() {
        _client.setEventSubscriber(this::acceptEvent);
        _client.setEventProducer(this::produceEvent);
    }

    /**
     * Reads in text from the console and returns null if it was a command
     * else returns a sendmessage event to be sent to the server.
     */
    private ChatEvent produceEvent() {
        var message = _input.nextLine();

        if (message.trim().length() == 0) {
            _displayOutput.reset();
            return null;
        }

        if (tryGetAndExecuteCommand(message)) {
            return null;
        }

        return ChatEventFactory.fromMessage(null, message);
    }

    /**
     * Accepts events from the socket and switches on the OpCode to handle various event types.
     */
    private void acceptEvent(ChatEvent event) {
        switch (SocketOpCode.fromValue(event.getOpCode())) {
            // Opening handshake
            case HELLO:
                // Respond with a USER_JOIN to log in
                _logger.debug("received server handshake");
                _client.sendEvent(ChatEventFactory.fromUserJoin(_config.getString("name", "Unnamed User")));
                break;
            // Received a message
            case MESSAGE:
                var messageArgs = (MessageSendEventArgs)event.getEventArgs();
                _displayOutput.write(String.format("<%s> %s", messageArgs.getAuthor(), messageArgs.getContent()));
                break;
            // A user joined the channel
            case USER_JOIN:
                var joinArgs = (UserJoinEventArgs)event.getEventArgs();
                _logger.info("%s joined the channel", joinArgs.getName());
                break;
            // A user left the channel
            case USER_LEAVE:
                var leaveArgs = (UserLeftEventArgs)event.getEventArgs();
                _logger.info("%s left the channel", leaveArgs.getName());
                break;
            // Received a direct message
            case DIRECT_MESSAGE:
                var dmArgs = (DmEventArgs)event.getEventArgs();
                _displayOutput.write(String.format("[%s -> %s] %s", dmArgs.getSenderName(), dmArgs.getTargetName(), dmArgs.getContent()));
                break;
            case GOODBYE:
                _logger.error("Server closed the connection");
                _client.dispose();
                System.exit(0);
                break;
            // Received an unexpected OpCode
            default:
                _logger.debug("received unknown opcode from server");
                break;
        }
    }

    /**
     * Attempts to find and execute a command.
     * @param message The string to try and find a command with.
     * @return Whether a command was found.
     */
    private boolean tryGetAndExecuteCommand(String message) {
        var tokens = message.split(" ");
        if (tokens.length < 1) {
            return false;
        }

        try {
            var context = new ClientCommandContext(this, _config, _logger);
            var result = _commands.execute(context, tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length)).get();

            if (result instanceof CommandNotFoundResult) {
                return false;
            }

            if (result.isSuccess()) {
                _logger.debug("%s executed successfully", tokens[0]);
            }
            else {
                _logger.warn("%s returned an error during execution: %s", result.getReason());
            }
            return true;
        }
        catch (ExecutionException | InterruptedException ignored) {
        }

        return false;
    }

    public static void main(String[] args) {
        var input = new Scanner(System.in);

        IOutputWriter display = new DisplayWriter(15);
        var logger = new DefaultLogger(ChatClient.class.getSimpleName(), Level.FINE, display);

        var config = new ConfigurationBuilder()
                .addConsole(args)
                .build();

        var helpMenu = new HelpMenuBuilder()
                .setTitle("==ChatClient==")
                .addItem("cca", "Sets the host of the server the client will attempt a connection to")
                .addItem("ccp", "Sets the port of the server the client will attempt a connection to")
                .addItem("name", "Sets the name to use for this session")
                .build();

        if (config.isSet("help")) {
            display = new ConsoleWriter();
            display.write(helpMenu);
            return;
        }

        if (!config.isSet("name")) {
            logger.info("you have not set a name. What would you like to call yourself?");
            config.setString("name", input.nextLine());
            logger.info("your name for this session was set to %s", config.getString("name"));
        }

        var client = new ChatClient(config, logger, display);

        try {
            client.connect();
        }
        catch (InterruptedException interruptEx) {
            logger.error("main client thread was interrupted");
        }
    }
}
