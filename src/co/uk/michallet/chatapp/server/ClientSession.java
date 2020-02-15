package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserChangeNameArgs;
import co.uk.michallet.chatapp.common.net.models.UserJoinEventArgs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSession implements Runnable {
    private final ILogger _logger;
    private final Socket _socket;
    private String _name;
    private final UserJoinEventArgs _joinEventArgs;
    private final ConcurrentMessageBus _messageBus;

    public ClientSession(ILogger logger, Socket socket, UserJoinEventArgs joinEventArgs, ConcurrentMessageBus messageBus) {
        _logger = logger;
        _socket = socket;
        _name = joinEventArgs.getName();
        _joinEventArgs = joinEventArgs;
        _messageBus = messageBus;
    }

    public String getName() {
        return _name;
    }

    public UserJoinEventArgs getJoinEvent() {
        return _joinEventArgs;
    }

    public synchronized void send(ChatEvent chatEvent) {
        try {
            var objectWriter = new ObjectOutputStream(_socket.getOutputStream());
            objectWriter.writeObject(chatEvent);
        }
        catch(Exception ex) {
            // swallowed, IOExceptions will be immediately thrown in run()- no need to handle them here.
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                var eventData = readEvent();

                if (eventData == null) {
                    _logger.debug("%s: null, aborting", _name);
                    return;
                }
                switch (SocketOpCode.fromValue(eventData.getOpCode())) {
                    case CHANGE_NAME:
                        _logger.debug("%s: change name", _name);
                        var nameArgs = (UserChangeNameArgs)eventData.getEventArgs();
                        nameArgs.setOldName(_name);
                        if (_messageBus.tryRename(nameArgs.getOldName(), nameArgs.getName())) {
                            _name = nameArgs.getName();
                            _messageBus.broadcast(eventData);
                        }
                        break;
                    case MESSAGE:
                        _logger.debug("%s: sent message", _name);
                        var messageArgs = (MessageSendEventArgs)eventData.getEventArgs();
                        messageArgs.setAuthor(_name);
                        _messageBus.broadcast(eventData);
                        break;
                    case GOODBYE:
                        _logger.debug("%s: sent goodbye", _name);
                        return;
                    default:
                        _logger.debug("%s: unknown", _name);
                        return;
                }
            }
        }
        catch (IOException | ClassNotFoundException ignored) {
        }
    }

    private ChatEvent readEvent() throws IOException, ClassNotFoundException {
        var objectReader = new ObjectInputStream(_socket.getInputStream());
        return (ChatEvent)objectReader.readObject();
    }

    public synchronized void close() throws IOException {
        _socket.close();
    }
}
