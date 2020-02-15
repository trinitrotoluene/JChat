package co.uk.michallet.chatapp.common.net.models;

import java.io.Serializable;

public class UserChangeNameArgs extends EventArgs {
    private String _oldName;
    private String _name;

    private static final long serialVersionUID = 95630736L;

    public String getOldName() {
        return _oldName;
    }

    public String getName() {
        return _name;
    }

    public void setOldName(String value) {
        _oldName = value;
    }

    public void setName(String value) {
        _name = value;
    }
}
