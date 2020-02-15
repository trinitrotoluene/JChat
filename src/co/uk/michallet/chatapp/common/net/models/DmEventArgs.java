package co.uk.michallet.chatapp.common.net.models;

public class DmEventArgs extends EventArgs {
    private String _targetName;
    private String _senderName;

    private static final long serialVersionUID = 8292646L;

    public String getTargetName() {
        return _targetName;
    }

    public String getSenderName() {
        return _senderName;
    }

    public void setTargetName(String value) {
        _targetName = value;
    }

    public void setSenderName(String value) {
        _senderName = value;
    }
}
