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

/**
 * Handles the handshake that upgrades the connection to a chat session
 */
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
            // Send a HELLO payload with no event data to the client
            _logger.debug("starting handshake");
            var helloEvent = ChatEventFactory.fromHello();
            writeEvent(helloEvent);
            _logger.debug("hello");

            // Expect to receive a USER_JOIN in response
            var userJoinEvent = readEvent();
            if (!(userJoinEvent.getEventArgs() instanceof UserJoinEventArgs) || SocketOpCode.fromValue(userJoinEvent.getOpCode()) != SocketOpCode.USER_JOIN) {
                _logger.debug("bad login message, aborting");
                _socket.close();
                return null;
            }

            // If we got one, spin up a new session and pass it back to the caller
            _logger.debug("%s: identify", ((UserJoinEventArgs)userJoinEvent.getEventArgs()).getName());
            return new ClientSession(_logger, _socket, (UserJoinEventArgs)userJoinEvent.getEventArgs(), _messageBus);
        }
        catch (IOException | ClassNotFoundException ioEx) {
            _logger.debug("exception during handshake: %s", ioEx.getMessage());
            return null;
        }
    }

    private void writeEvent(ChatEvent event) throws IOException {
        // Wrap the Socket's OutputStream in an ObjectOutputStream and serialize a POJO to it
        var objectWriter = new ObjectOutputStream(_socket.getOutputStream());
        objectWriter.writeObject(event);
        // Flush any buffered bytes to the underlying stream
        objectWriter.flush();
    }

    private ChatEvent readEvent() throws IOException, ClassNotFoundException {
        // Wrap the byte stream coming in over the socket in an ObjectInputStream
        var objectReader = new ObjectInputStream(_socket.getInputStream());
        // Deserialize them into a ChatEvent
        return (ChatEvent)objectReader.readObject();
    }
}
