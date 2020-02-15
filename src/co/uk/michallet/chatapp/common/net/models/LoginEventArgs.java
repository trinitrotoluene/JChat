package co.uk.michallet.chatapp.common.net.models;

public class LoginEventArgs extends EventArgs {
    private String _username;
    private String _password;

    private static final long serialVersionUID = 2589403L;

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public void setUsername(String value) {
        _username = value;
    }

    public void setPassword(String value) {
        _password = value;
    }
}
