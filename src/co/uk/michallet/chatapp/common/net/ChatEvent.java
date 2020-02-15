package co.uk.michallet.chatapp.common.net;

import co.uk.michallet.chatapp.common.net.models.EventArgs;

import java.io.Serializable;

public class ChatEvent implements Serializable {
    private EventArgs _eventArgs;
    private int _opCode;

    public EventArgs getEventArgs() {
        return _eventArgs;
    }

    public int getOpCode() {
        return _opCode;
    }

    public void setEventArgs(EventArgs value) {
        _eventArgs = value;
    }

    public void setOpCode(int value) {
        _opCode = value;
    }
}
