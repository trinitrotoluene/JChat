package co.uk.michallet.chatapp.common.SDK;

import co.uk.michallet.chatapp.common.AppThreadPool;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.net.ChatEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic client implementation for use in a variety of scenarios.
 */
public class GenericClient {
    private Consumer<ChatEvent> _eventHandler;
    private Supplier<ChatEvent> _eventProducer;
    private Socket _socket;
    private final ILogger _logger;

    public GenericClient(ILogger logger) {
        _logger = logger;
    }

    public void setEventSubscriber(Consumer<ChatEvent> eventHandler) {
        _eventHandler = eventHandler;
    }

    public void setEventProducer(Supplier<ChatEvent> eventProducer) {
        _eventProducer = eventProducer;
    }

    /**
     * Attempts to connect to a server on the supplied host and port.
     */
    public void connect(InetAddress addr, int port) throws IOException {
        _socket = new Socket(addr, port);
        Runtime.getRuntime().addShutdownHook(new Thread(AppThreadPool.getInstance()::shutdown));
    }

    /**
     * Asynchronously run the producing and listening loops of the client on common ForkJoinPool threads.
     * @return A future that completes when the socket is closed or one of the loops encounters an exception.
     */
    public CompletableFuture<Void> runAsync() {
        // Set a hook here to account for sudden external shutdowns.
        Runtime.getRuntime().addShutdownHook(new Thread(this::dispose));
        // Spin up the producing (sending) loop
        var producingFuture = CompletableFuture.runAsync(() -> {
            while(_socket.isConnected() && !Thread.interrupted()) {
                // Async read from the provided Producer<ChatEvent> and pass it to sendEvent() in a continuation.
                var iterationFuture = CompletableFuture.supplyAsync(_eventProducer)
                    .thenAcceptAsync(this::sendEvent);

                try {
                    // Block on it so we aren't trying to write in parallel
                    iterationFuture.get();
                }
                catch (InterruptedException interruptEx) {
                    // Don't lose the IRQ for when we loop around again
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException ignored) {
                }
            }
        }, AppThreadPool.getInstance());
        // Spin up the dispatching (receiving) loop
        var dispatchingFuture = CompletableFuture.runAsync(() -> {
            while (!_socket.isClosed() && !Thread.interrupted()) {
                // Asynchronously return a ChatEvent to the chained continuation (_eventHandler)
                var iterationFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        var reader = new ObjectInputStream(_socket.getInputStream());
                        return (ChatEvent)reader.readObject();
                    }
                    catch (IOException | ClassNotFoundException ioEx) {
                        // Climate change is endangering the IRQ, let's save it
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }).thenAcceptAsync(_eventHandler);

                try {
                    // Block on it to prevent concurrent socket reads.
                    iterationFuture.get();
                }
                catch (InterruptedException interruptEx) {
                    // Don't lose the IRQ 2: Electric boogaloo
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException ignored) {
                }
            }
        }, AppThreadPool.getInstance());

        // Wrap them both info a future the caller can block on or chain their own continuation to.
        return CompletableFuture.allOf(producingFuture, dispatchingFuture);
    }

    /**
     * Sends event data over the socket.
     */
    public synchronized void sendEvent(ChatEvent event) {
        if (event == null) {
            return;
        }

        try {
            var writer = new ObjectOutputStream(_socket.getOutputStream());
            writer.writeObject(event);
            writer.flush();
        }
        catch (IOException ignored) {
        }
    }

    public synchronized void dispose() {
        try {
            if (_socket.isClosed()) {
                return;
            }
            _logger.info("disconnecting");
            _socket.close();
            _logger.info("disconnected");
        }
        catch (IOException ioEx) {
            _logger.error("an exception was thrown while trying to close the socket: %s", ioEx.getMessage());
        }
    }
}
