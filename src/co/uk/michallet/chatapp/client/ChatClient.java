package co.uk.michallet.chatapp.client;

import co.uk.michallet.chatapp.common.ConfigurationBuilder;
import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.IOutputWriter;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.SDK.GenericClient;
import co.uk.michallet.chatapp.common.logging.DefaultLogger;
import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserJoinEventArgs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class ChatClient {
    private IConfiguration _config;
    private ILogger _logger;
    private IOutputWriter _displayOutput;
    private GenericClient _client;
    private CompletableFuture<Void> _listenTask;

    private final Scanner _input = new Scanner(System.in);

    private int _backoff = 2;
    private final int BACKOFF_FACTOR = 2;

    public ChatClient(IConfiguration config, ILogger logger, IOutputWriter displayOutput) {
        _config = config;
        _logger = logger;
        _displayOutput = displayOutput;
        _client = new GenericClient(logger);
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

        hookClient();

        for (;;) {
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
            _listenTask = _client.runAsync();
            _listenTask.get();
        }
        catch (ExecutionException e) {
            _logger.error("the server connection was aborted");
        }

        _client.dispose();
    }

    private void hookClient() {
        _client.setEventSubscriber(this::acceptEvent);
        _client.setEventProducer(this::produceEvent);
    }

    public ChatEvent produceEvent() {
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

    private void acceptEvent(ChatEvent event) {
        switch (SocketOpCode.fromValue(event.getOpCode())) {
            case HELLO:
                _logger.debug("received server handshake");
                _client.sendEvent(ChatEventFactory.fromUserJoin(_config.getString("name", "Unnamed User")));
                break;
            case MESSAGE:
                var messageArgs = (MessageSendEventArgs)event.getEventArgs();
                _displayOutput.write(String.format("<%s> %s", messageArgs.getAuthor(), messageArgs.getContent()));
                break;
            case USERJOIN:
                var joinArgs = (UserJoinEventArgs)event.getEventArgs();
                _logger.info("%s joined the channel", joinArgs.getName());
            default:
                _logger.debug("received unknown opcode from server");
        }
    }

    private boolean tryGetAndExecuteCommand(String message) {
        var tokens = message.split(" ");
        if (tokens.length < 1) {
            return false;
        }
        switch (tokens[0]) {
            case "RENAME":
                if (tokens.length < 2) {
                    return false;
                }
                var renameEvent = ChatEventFactory.fromNameChange("", tokens[1]);
                _client.sendEvent(renameEvent);
                return true;
            case "EXIT":
            case "DISCONNECT":
                _listenTask.cancel(true);
                break;
        }

        return false;
    }

    public static void main(String[] args) {
        var input = new Scanner(System.in);

        var display = new DisplayWriter(15);
        var logger = new DefaultLogger(ChatClient.class.getSimpleName(), Level.FINE, display);

        var config = new ConfigurationBuilder()
                .addConsole(args)
                .build();

        if (config.isSet("help")) {
            printHelp(display);
            return;
        }

        if (!config.isSet("name")) {
            logger.info("you have not set a name. What would you like to call yourself?");
            config.setString("name", input.nextLine());
            logger.info("your name for this session was set to %s", config.getString("name"));
        }

        if (!config.isSet("password")) {
            logger.warn("you have not set a password for this session. If you do not set a password then your chosen name will not be protected when you reconnect");
            logger.warn("run the client with the --help flag set for more information");
        }

        var client = new ChatClient(config, logger, display);

        try {
            client.connect();
        }
        catch (InterruptedException interruptEx) {
            logger.error("main client thread was interrupted");
        }
    }

    private static void printHelp(IOutputWriter display) {
        display.write("                 == Usage Instructions for ChatServer ==");
        display.write("");
        display.write("All options can be set in the GNU or POSIX formats i.e. -cca <value> --cca=<value>");
        display.write("cca       | Sets the host of the server the client will attempt a connection to.");
        display.write("ccp       | Sets the port of the server the client will attempt a connection to.");
        display.write("help      | Displays this menu");
        display.write("name      | Sets a name to use for the session.");
        display.write("password  | The password to use when claiming or re-authenticating a name with the server.");
    }
}
