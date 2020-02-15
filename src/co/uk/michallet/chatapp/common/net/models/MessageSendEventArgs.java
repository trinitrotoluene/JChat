package co.uk.michallet.chatapp.common.net.models;

public class MessageSendEventArgs extends EventArgs {
    private String _content;
    private String _author;

    private static final long serialVersionUID = 38797114L;

    public String getContent() {
        return _content;
    }

    public String getAuthor() {
        return _author;
    }

    public void setContent(String value) {
        _content = value;
    }

    public void setAuthor(String value) {
        _author = value;
    }
}
