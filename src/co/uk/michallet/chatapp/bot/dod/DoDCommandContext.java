package co.uk.michallet.chatapp.bot.dod;

/**
 * Provides contextual information to commands during execution.
 */
public class DoDCommandContext {
    private final PlayerBase _sender;
    private final GameMap _map;
    private final String[] _args;
    private final Game _game;

    /**
     * Create a new context including player, map, argument and game data information.
     * @param sender The entity invoking the command.
     * @param map The map that the game is currently running on.
     * @param args An array of arguments provided to the command.
     * @param game The game that this command is being executed for.
     */
    public DoDCommandContext(PlayerBase sender, GameMap map, String[] args, Game game) {
        this._sender = sender;
        this._map = map;
        this._args = args;
        this._game = game;
    }

    /**
     * @return The command sender.
     */
    public PlayerBase getSender() {
        return this._sender;
    }

    /**
     * @return The map from context.
     */
    public GameMap getMap() {
        return this._map;
    }

    /**
     * @return The text arguments provided to the command.
     */
    public String[] getArgs() {
        return this._args;
    }

    /**
     * @return The current game from context.
     */
    public Game getGame() {
        return _game;
    }
}
