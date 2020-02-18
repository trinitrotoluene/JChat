package co.uk.michallet.chatapp.common.net.models;

public class DmEventArgs extends EventArgs {
    private String _targetName;
    private String _senderName;
    private String _content;

    private static final long serialVersionUID = 8292646L;

    public String getTargetName() {
        return _targetName;
    }

    public String getSenderName() {
        return _senderName;
    }

    public String getContent() {
        return _content;
    }

    public void setTargetName(String value) {
        _targetName = value;
    }

    public void setSenderName(String value) {
        _senderName = value;
    }

    public void setContent(String value) {
        _content = value;
    }
}
