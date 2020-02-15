package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.UserJoinEventArgs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Supplier;

public class ClientSessionNegotiator implements Supplier<ClientSession> {
    private final ILogger _logger;
    private final Socket _socket;
    private final ConcurrentMessageBus _messageBus;

    public ClientSessionNegotiator(ILogger logger, Socket socket, ConcurrentMessageBus messageBus) {
        _logger = logger;
        _socket = socket;
        _messageBus = messageBus;
    }

    @Override
    public ClientSession get() {
        try {
            _logger.debug("starting handshake");
            var helloEvent = ChatEventFactory.fromHello();
            writeEvent(helloEvent);
            _logger.debug("hello");

            var userJoinEvent = readEvent();
            if (userJoinEvent.getEventArgs() == null || SocketOpCode.fromValue(userJoinEvent.getOpCode()) != SocketOpCode.USERJOIN) {
                _logger.debug("bad login message, aborting");
                _socket.close();
                return null;
            }

            _logger.debug("%s: identify", ((UserJoinEventArgs)userJoinEvent.getEventArgs()).getName());
            // todo: validate login
            // todo: send back indication of success
            return new ClientSession(_logger, _socket, (UserJoinEventArgs)userJoinEvent.getEventArgs(), _messageBus);
        }
        catch (IOException | ClassNotFoundException ioEx) {
            _logger.debug("exception during handshake: %s", ioEx.getMessage());
            return null;
        }
    }

    private void writeEvent(ChatEvent event) throws IOException {
        var objectWriter = new ObjectOutputStream(_socket.getOutputStream());
        objectWriter.writeObject(event);
        objectWriter.flush();
    }

    private ChatEvent readEvent() throws IOException, ClassNotFoundException {
        var objectReader = new ObjectInputStream(_socket.getInputStream());
        return (ChatEvent)objectReader.readObject();
    }
}
