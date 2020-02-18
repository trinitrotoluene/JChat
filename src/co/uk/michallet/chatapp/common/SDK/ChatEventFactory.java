package co.uk.michallet.chatapp.common.SDK;

import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.DmEventArgs;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserChangeNameArgs;
import co.uk.michallet.chatapp.common.net.models.UserJoinEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserLeftEventArgs;

public class ChatEventFactory {
    private ChatEventFactory() {
    }

    public static ChatEvent fromDM(String sender, String target, String content) {
        var event = new ChatEvent();
        var eventArgs = new DmEventArgs();
        eventArgs.setSenderName(sender);
        eventArgs.setTargetName(target);
        eventArgs.setContent(content);
        event.setOpCode(SocketOpCode.DIRECT_MESSAGE.getValue());
        event.setEventArgs(eventArgs);

        return event;
    }

    public static ChatEvent fromMessage(String author, String message) {
        var event = new ChatEvent();
        var eventArgs = new MessageSendEventArgs();
        eventArgs.setAuthor(author);
        eventArgs.setContent(message);
        event.setOpCode(SocketOpCode.MESSAGE.getValue());
        event.setEventArgs(eventArgs);

        return event;
    }

    public static ChatEvent fromNameChange(String oldName, String newName) {
        var event = new ChatEvent();
        var eventArgs = new UserChangeNameArgs();
        eventArgs.setOldName(oldName);
        eventArgs.setName(newName);
        event.setOpCode(SocketOpCode.CHANGE_NAME.getValue());
        event.setEventArgs(eventArgs);

        return event;
    }

    public static ChatEvent fromUserJoin(String name) {
        var event = new ChatEvent();
        var eventArgs = new UserJoinEventArgs();
        eventArgs.setName(name);
        event.setOpCode(SocketOpCode.USER_JOIN.getValue());
        event.setEventArgs(eventArgs);

        return event;
    }

    public static ChatEvent fromUserLeave(String name) {
        var event = new ChatEvent();
        var eventArgs = new UserLeftEventArgs();
        eventArgs.setName(name);
        event.setOpCode(SocketOpCode.USER_LEAVE.getValue());
        event.setEventArgs(eventArgs);

        return event;
    }

    public static ChatEvent fromHello() {
        var event = new ChatEvent();
        event.setOpCode(SocketOpCode.HELLO.getValue());

        return event;
    }
}
