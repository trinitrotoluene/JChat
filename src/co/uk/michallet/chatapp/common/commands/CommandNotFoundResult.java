package co.uk.michallet.chatapp.common.commands;

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
