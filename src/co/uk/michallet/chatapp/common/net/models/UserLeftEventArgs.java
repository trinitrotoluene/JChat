package co.uk.michallet.chatapp.common.net.models;

public class UserLeftEventArgs extends EventArgs {
    private String _name;

    private static final long serialVersionUID = 5699577L;

    public String getName() {
        return _name;
    }

    public void setName(String value) {
        _name = value;
    }
}
