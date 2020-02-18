package co.uk.michallet.chatapp.bot.dod;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Composes package functionality to implement the "Dungeon of Doom".
 */
public final class Game {
    private final GameMap _map;
    private final PlayerBase[] _players;
    private final CommandHandler _commands;

    private Boolean _completed = false;
    private Boolean _won = false;

    /**
     * Create a new Game.
     * @param map Map to use for the game.
     * @param players Players participating in the game.
     */
    public Game(GameMap map, PlayerBase[] players) {
        this._map = map;
        this._players = players;
        this._commands = new CommandHandler();
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
            if (player instanceof Player)
                tile.setFlag(TileFlags.PLAYER);
            else
                tile.setFlag(TileFlags.BOT);
        }

        while (!this._completed) {
            for (var player : this._players) {
                var rawInput = player.getNextCommand()
                        .split(" ");

                var command = Arrays.stream(rawInput)
                        .filter(s -> !s.isEmpty()).findFirst()
                        .orElse(""); // Invalid commands are handled elsewhere.
                var args = Arrays.stream(rawInput)
                        .skip(1)
                        .collect(Collectors.toList())
                        .toArray(new String[0]);

                this.executeCommand(player, command, args);
            }
        }
    }

    private void executeCommand(PlayerBase sender, String commandName, String[] args) {
        this._commands.execute(commandName, new DoDCommandContext(sender, this._map, args, this));
    }
}
