package co.uk.michallet.chatapp.bot;

import co.uk.michallet.chatapp.bot.dod.PlayerBase;
import co.uk.michallet.chatapp.bot.dod.Point2D;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class NetworkedPlayer extends PlayerBase {
    private final DoDClient _client;
    private final String _name;

    private CompletableFuture<String> _nextCommandFuture;

    public NetworkedPlayer(DoDClient client, String name, Point2D pos) {
        super(pos);
        _client = client;
        _name = name;
        _nextCommandFuture = new CompletableFuture<>();
    }

    public String getName() {
        return _name;
    }

    @Override
    public String getNextCommand() {
        String nextCommand = "";
        try {
            _client.getLogger().debug("Game waiting for next command from %s", _name);
            // Block until the client thread completes the future
            nextCommand = _nextCommandFuture.get();
        }
        catch (InterruptedException | ExecutionException ignored) {
        }
        finally {
            _nextCommandFuture = new CompletableFuture<>();
        }

        return nextCommand.toLowerCase();
    }

    @Override
    public void sendMessage(String format, Object... args) {
        // Send a private message to the user
        _client.sendEvent(ChatEventFactory.fromDM("", _name, String.format(format, args)));
    }

    /**
     * Complete the future the Game is blocking on in getNextCommand
     * @param message the value the future is completed with
     */
    public void putNextCommand(String message) {
        _nextCommandFuture.complete(message);
    }
}