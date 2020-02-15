package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.net.ChatEvent;
import co.uk.michallet.chatapp.common.net.SocketOpCode;
import co.uk.michallet.chatapp.common.net.models.EventArgs;
import co.uk.michallet.chatapp.common.net.models.MessageSendEventArgs;
import co.uk.michallet.chatapp.common.net.models.UserJoinEventArgs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConcurrentMessageBus {
    private Map<String, ClientSession> _subscribers;

    public ConcurrentMessageBus() {
        _subscribers = new HashMap<>();
    }

    private synchronized boolean getIsNameTaken(String name) {
        return _subscribers.containsKey(name);
    }

    public synchronized boolean tryRename(String oldName, String newName) {
        if (getIsNameTaken(newName)) {
            return false;
        }

        var session = _subscribers.remove(oldName);
        _subscribers.put(newName, session);
        return true;
    }

    public synchronized void broadcast(ChatEvent event) {
        for (var client : _subscribers.values()) {
            CompletableFuture.runAsync(() -> {
                client.send(event);
            });
        }
    }

    public synchronized boolean tryAddClient(ClientSession session) {
        if (getIsNameTaken(session.getName())) {
            return false;
        }

        _subscribers.put(session.getName(), session);
        return true;
    }

    public synchronized void removeClient(ClientSession session) {
        _subscribers.remove(session.getName());
    }
}
