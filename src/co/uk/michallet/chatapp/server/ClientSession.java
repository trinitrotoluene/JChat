package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.DmEventArgs;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserChangeNameArgs;
import co.uk.michallet.chatapp.common.net.models.UserJoinEventArgs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Represents the session of a connected client.
 */
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

    /**
     * Sends an event to this session.
     * @param chatEvent The event payload to send.
     */
    public synchronized void send(ChatEvent chatEvent) {
        try {
            var objectWriter = new ObjectOutputStream(_socket.getOutputStream());
            objectWriter.writeObject(chatEvent);
        }
        catch(Exception ex) {
            // swallowed, IOExceptions will be immediately thrown in run()- no need to handle them here.
        }
    }

    /**
     * Called when the session has been established and the server is ready to read in messages from the client.
     */
    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && !_socket.isClosed()) {
                var eventData = readEvent();

                if (eventData == null) {
                    _logger.debug("%s: null, aborting", _name);
                    return;
                }
                // Switch on the opcode of the event we received
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
                        messageArgs.setAuthor(_name); // Never trust the client. Set the name associated with the session server-side.
                        _messageBus.broadcast(eventData);
                        break;
                    case DIRECT_MESSAGE:
                        _logger.debug("%s: sent direct message", _name);
                        var dmArgs = (DmEventArgs)eventData.getEventArgs();
                        var target = _messageBus.getClient(dmArgs.getTargetName());
                        if (target != null) {
                            dmArgs.setSenderName(_name); // Never trust the client 2: electric boogaloo
                            target.send(eventData);
                            send(eventData);
                        }
                        break;
                    case GOODBYE:
                        _logger.debug("%s: sent goodbye", _name);
                        return;
                    default:
                        _logger.debug("%s: unknown opcode", _name);
                        break;
                }
            }
        }
        catch (IOException | ClassNotFoundException ignored) {
        }
    }

    private ChatEvent readEvent() throws IOException, ClassNotFoundException {
        // Wrap the socket inputstream in an object reader
        var objectReader = new ObjectInputStream(_socket.getInputStream());
        // Deserialize the bytes into a ChatEvent POJO
        return (ChatEvent)objectReader.readObject();
    }

    /**
     * Close the socket
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        _socket.close();
    }
}
