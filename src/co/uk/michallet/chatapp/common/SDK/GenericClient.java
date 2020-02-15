package co.uk.michallet.chatapp.common.SDK;

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

    public void connect(InetAddress addr, int port) throws IOException {
        _socket = new Socket(addr, port);
        Runtime.getRuntime().addShutdownHook(new Thread(this::dispose));
    }

    public CompletableFuture<Void> runAsync() {
        var producingFuture = CompletableFuture.runAsync(() -> {
            while(_socket.isConnected() && !Thread.interrupted()) {
                var iterationFuture = CompletableFuture.supplyAsync(_eventProducer)
                    .thenAcceptAsync(this::sendEvent);

                try {
                    iterationFuture.get();
                }
                catch (InterruptedException interruptEx) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException ignored) {
                }
            }
        });

        var dispatchingFuture = CompletableFuture.runAsync(() -> {
            while (_socket.isConnected() && !Thread.interrupted()) {
                var iterationFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        var reader = new ObjectInputStream(_socket.getInputStream());
                        return (ChatEvent)reader.readObject();
                    }
                    catch (IOException | ClassNotFoundException ioEx) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }).thenAcceptAsync(_eventHandler);

                try {
                    iterationFuture.get();
                }
                catch (InterruptedException interruptEx) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException ignored) {
                }
            }
        });

        return CompletableFuture.allOf(producingFuture, dispatchingFuture);
    }

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

    public void dispose() {
        try {
            _logger.info("disconnecting");
            _socket.close();
            _logger.info("disconnected");
        }
        catch (IOException ioEx) {
            _logger.error("an exception was thrown while trying to close the socket: %s", ioEx.getMessage());
        }
    }
}
