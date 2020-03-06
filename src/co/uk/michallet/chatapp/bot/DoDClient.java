package co.uk.michallet.chatapp.bot;

import co.uk.michallet.chatapp.bot.dod.DoDLoader;
import co.uk.michallet.chatapp.bot.dod.Game;
import co.uk.michallet.chatapp.bot.dod.MapUtil;
import co.uk.michallet.chatapp.bot.dod.PlayerBase;
import co.uk.michallet.chatapp.bot.dod.Point2D;
import co.uk.michallet.chatapp.bot.dod.TileFlags;
import co.uk.michallet.chatapp.common.ConfigurationBuilder;
import co.uk.michallet.chatapp.common.ConsoleWriter;
import co.uk.michallet.chatapp.common.HelpMenuBuilder;
import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.SDK.GenericClient;
import co.uk.michallet.chatapp.common.logging.DefaultLogger;
import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * Modified version of the ChatBot that allows you to play the DoD game.
 */
public class DoDClient {
    private int _backoff;
    private final int BACKOFF_FACTOR = 2;

    private final IConfiguration _config;
    private final DefaultLogger _logger;
    private final GenericClient _client;
    private final Game _game;

    private CompletableFuture<Void> _listenTask;
    private CompletableFuture<Void> _gameTask;

    public DoDClient(IConfiguration config, DefaultLogger logger, Game game) {
        _config = config;
        _logger = logger;
        _game = game;

        _client = new GenericClient(_logger);
    }

    public void sendEvent(ChatEvent event) {
        _client.sendEvent(event);
    }

    public ILogger getLogger() {
        return _logger;
    }

    public void connect() throws InterruptedException {
        _gameTask = CompletableFuture.runAsync(_game::run);

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

    private void acceptCommands(ChatEvent event) {
        switch (SocketOpCode.fromValue(event.getOpCode())) {
            case HELLO:
                _logger.debug("Received server handshake");
                _client.sendEvent(ChatEventFactory.fromUserJoin(_config.getString("name", "DoDClient")));
                return;
            case MESSAGE:
                var eventArgs = (MessageSendEventArgs)event.getEventArgs();
                _logger.debug("%s: %s", eventArgs.getAuthor(), eventArgs.getContent());

                // If the Game is currently waiting for a player's input
                if (_game.getCurrentPlayer() instanceof NetworkedPlayer) {
                    var player = (NetworkedPlayer)_game.getCurrentPlayer();
                    // Check whether the player we're waiting for is the one sending this message
                    if (eventArgs.getAuthor().equals(player.getName())) {
                        _logger.debug("Put next command for player %s: %s", eventArgs.getAuthor(), eventArgs.getContent());
                        player.putNextCommand(eventArgs.getContent());
                        return;
                    }
                }

                if (eventArgs.getContent().toLowerCase().equals("join")) {
                    _logger.info("%s tried to join the game", eventArgs.getAuthor());
                    // Ensure the user isn't already playing
                    for (var player : _game.getPlayers()) {
                        if (player instanceof NetworkedPlayer && ((NetworkedPlayer)player).getName().equals(eventArgs.getAuthor())) {
                            return;
                        }
                    }
                    // Get all the positions of currently playing users
                    var positions = _game.getPlayers().stream()
                            .map(PlayerBase::getPos)
                            .toArray(Point2D[]::new);
                    // Calculate a valid starting point
                    var pos = MapUtil.getLegalStartingPoint(_game.getMap(), positions);
                    _game.getPlayers().add(
                            new NetworkedPlayer(this, eventArgs.getAuthor(), pos)
                    );
                    // Set the flags for the tile of player we've just placed
                    var tile = _game.getMap().getTileAt(pos);
                    tile.setFlag(TileFlags.PLAYER);
                    _client.sendEvent(ChatEventFactory.fromDM("", eventArgs.getAuthor(), String.format("You've joined the dungeon!", eventArgs.getAuthor())));
                }
                return;
            default:
                return;
        }
    }

    public static void main(String[] args) {
        var display = new ConsoleWriter();
        var logger = new DefaultLogger(DoDClient.class.getSimpleName(), Level.FINE, display);

        var config = new ConfigurationBuilder()
                .addConsole(args)
                .build();

        var helpMenu = new HelpMenuBuilder()
                .setTitle("==DoDClient==")
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
            config.setString("name", "DoDClient");
        }

        var loader = new DoDLoader();
        var game = loader.run();
        if (game == null) {
            return;
        }

        var bot = new DoDClient(config, logger, game);

        try {
            bot.connect();
        }
        catch (InterruptedException interruptEx) {
            logger.error("main client thread was interrupted");
        }
    }
}
