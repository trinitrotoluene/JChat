package co.uk.michallet.chatapp.common.net.models;

import java.io.Serializable;

public class UserJoinEventArgs extends EventArgs {
    private String _name;

    private static final long serialVersionUID = 720599L;

    public String getName() {
        return _name;
    }

    public void setName(String value) {
        _name = value;
    }
}
