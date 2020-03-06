package co.uk.michallet.chatapp.bot.dod;

import co.uk.michallet.chatapp.bot.NetworkedPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Composes package functionality to implement the "Dungeon of Doom".
 */
public final class Game {
    private final GameMap _map;
    private final Queue<PlayerBase> _players;
    private final CommandHandler _commands;

    private Boolean _completed = false;
    private Boolean _won = false;
    private PlayerBase _currentPlayer;

    /**
     * Create a new Game.
     * @param map Map to use for the game.
     * @param players Players participating in the game.
     */
    public Game(GameMap map, Queue<PlayerBase> players) {
        this._map = map;
        this._players = players;
        this._commands = new CommandHandler();
    }

    public Queue<PlayerBase> getPlayers() {
        return this._players;
    }

    public PlayerBase getCurrentPlayer() {
        return this._currentPlayer;
    }

    public GameMap getMap() {
        return this._map;
    }

    /**
     * Sets the game to be completed.
     */
    public void setCompleted() {
        this._completed = true;
    }

    /**
     * Sets the game to be won by the player.
     */
    public void setWon() {
        this._won = true;
    }

    /**
     * @return Whether the game is won.
     */
    public Boolean isWon() {
        return this._won;
    }

    /**
     * Blocks while the game is running, accepts user input and executes commands.
     */
    public void run() {
        for (var player : this._players) {
            var tile = this._map.getTileAt(player);
            if (player instanceof Player || player instanceof NetworkedPlayer)
                tile.setFlag(TileFlags.PLAYER);
            else
                tile.setFlag(TileFlags.BOT);
        }

        while (!this._completed) {
            for (var player : this._players) {
                this._currentPlayer = player;
                var rawInput = player.getNextCommand().split(" ");

                var command = Arrays.stream(rawInput)
                        .filter(s -> !s.isEmpty()).findFirst()
                        .orElse(""); // Invalid commands are handled elsewhere.
                var args = Arrays.stream(rawInput)
                        .skip(1).toArray(String[]::new);

                this.executeCommand(player, command, args);
            }
        }

        if (this._won) {
            UI.win();
        }
        else {
            UI.lose();
        }
    }

    private void executeCommand(PlayerBase sender, String commandName, String[] args) {
        this._commands.execute(commandName, new DoDCommandContext(sender, this._map, args, this));
    }
}
