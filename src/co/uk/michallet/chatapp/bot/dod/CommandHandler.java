package co.uk.michallet.chatapp.bot.dod;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores and routes input to user-invoked commands.
 */
public final class CommandHandler {
    private Map<String, GameCommand> _commands = new HashMap<>();

    /**
     * Construct a new {@link CommandHandler} and hook commands.
     */
    public CommandHandler() {
        this._commands.put("hello", HelloCommand.getInstance());
        this._commands.put("pickup", PickupCommand.getInstance());
        this._commands.put("gold", GoldCommand.getInstance());
        this._commands.put("move", MoveCommand.getInstance());
        this._commands.put("look", LookCommand.getInstance());
        this._commands.put("quit", QuitCommand.getInstance());
    }

    /**
     * Executes a command, passing it the provided context.
     * @param command The command's name.
     * @param context The context that the command should operate on.
     */
    public void execute(String command, DoDCommandContext context) {
        if (_commands.containsKey(command)) {
            _commands.get(command).execute(context);
        }
        else {
            context.getSender().sendMessage("FAIL: Unknown command.");
        }
    }
}
