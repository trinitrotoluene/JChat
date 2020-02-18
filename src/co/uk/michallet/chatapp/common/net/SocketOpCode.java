package co.uk.michallet.chatapp.common.net;

public enum SocketOpCode {
    HELLO(0),
    GOODBYE(1),
    USER_JOIN(2),
    USER_LEAVE(3),
    MESSAGE(4),
    DIRECT_MESSAGE(5),
    CHANGE_NAME(8);

    private final int _value;

    SocketOpCode(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }

    public static SocketOpCode fromValue(int value) {
        switch (value) {
            case 0:
                return SocketOpCode.HELLO;
            case 1:
                return SocketOpCode.GOODBYE;
            case 2:
                return SocketOpCode.USER_JOIN;
            case 3:
                return SocketOpCode.USER_LEAVE;
            case 4:
                return SocketOpCode.MESSAGE;
            case 5:
                return SocketOpCode.DIRECT_MESSAGE;
            case 8:
                return SocketOpCode.CHANGE_NAME;
        }

        throw new IllegalArgumentException();
    }
}
