package co.uk.michallet.chatapp.bot;

import co.uk.michallet.chatapp.common.ConfigurationBuilder;
import co.uk.michallet.chatapp.common.ConsoleWriter;
import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.IOutputWriter;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.SDK.GenericClient;
import co.uk.michallet.chatapp.common.commands.CommandService;
import co.uk.michallet.chatapp.common.logging.DefaultLogger;
import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class ChatBot {
    private int _backoff;
    private final int BACKOFF_FACTOR = 2;

    private final GenericClient _client;
    private final ILogger _logger;
    private final IConfiguration _config;
    private final CommandService<BotCommandContext> _commands;

    private CompletableFuture<Void> _listenTask;

    public ChatBot(IConfiguration config, ILogger logger) {
        _logger = logger;
        _config = config;
        _client = new GenericClient(_logger);
        _commands = new CommandService<>();
        _commands.registerCommands(BotCommands.class);
    }

    public synchronized void cancel() {
        if (_listenTask != null) {
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
        _client.setEventProducer(() -> {
            try {
                new CyclicBarrier(2).await();
            }
            catch (InterruptedException | BrokenBarrierException iDislikeCheckedExceptions) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
        _client.setEventSubscriber(this::acceptCommands);
    }

    private void acceptCommands(ChatEvent chatEvent) {
        switch (SocketOpCode.fromValue(chatEvent.getOpCode())) {
            case HELLO:
                _logger.debug("received server handshake");
                _client.sendEvent(ChatEventFactory.fromUserJoin(_config.getString("name", "Unnamed Bot")));
                break;
            case MESSAGE:
                var eventArgs = (MessageSendEventArgs)chatEvent.getEventArgs();
                var commandTokens = eventArgs.getContent().split(" ");
                try {
                    var context = new BotCommandContext(this, _config, _logger);
                    var result = _commands.execute(context, commandTokens[0], Arrays.copyOfRange(commandTokens, 1, commandTokens.length)).get();
                }
                catch (InterruptedException | ExecutionException ignored) {
                }
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        var input = new Scanner(System.in);

        var display = new ConsoleWriter();
        var logger = new DefaultLogger(ChatBot.class.getSimpleName(), Level.FINE, display);

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

        var bot = new ChatBot(config, logger);

        try {
            bot.connect();
        }
        catch (InterruptedException interruptEx) {
            logger.error("main client thread was interrupted");
        }
    }

    private static void printHelp(IOutputWriter display) {
        display.write("                 == Usage Instructions for ChatBot ==");
        display.write("");
        display.write("All options can be set in the GNU or POSIX formats i.e. -cca <value> --cca=<value>");
        display.write("cca       | Sets the host of the server the client will attempt a connection to.");
        display.write("ccp       | Sets the port of the server the client will attempt a connection to.");
        display.write("help      | Displays this menu");
        display.write("name      | Sets a name to use for the session.");
    }
}
