package co.uk.michallet.chatapp.common.commands;

public class ExecutionResult implements IResult {
    private boolean _isSuccess;
    private String _reason;

    public ExecutionResult(boolean isSuccess) {
        _isSuccess = isSuccess;
        _reason = "";
    }

    public ExecutionResult(boolean isSuccess, String reason) {
        _isSuccess = isSuccess;
        _reason = reason;
    }

    @Override
    public boolean isSuccess() {
        return _isSuccess;
    }

    @Override
    public String getReason() {
        return _reason;
    }
}
