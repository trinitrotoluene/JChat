package co.uk.michallet.chatapp.common.commands;

/**
 * Custom result returned by the CommandService when a command was not found matching the desired token.
 */
public class CommandNotFoundResult implements IResult {
    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getReason() {
        return "command not found";
    }
}
