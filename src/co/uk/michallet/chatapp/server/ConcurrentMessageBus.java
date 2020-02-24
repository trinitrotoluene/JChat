package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.net.ChatEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a list of subscribed sessions and broadcasts messages to them
 */
public class ConcurrentMessageBus {
    private Map<String, ClientSession> _subscribers;

    public ConcurrentMessageBus() {
        // We need to use a concurrent collection here otherwise getNames() is not threadsafe
        _subscribers = new ConcurrentHashMap<>();
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

    public synchronized ClientSession getClient(String name) {
        return _subscribers.getOrDefault(name, null);
    }

    public synchronized Set<String> getNames() {
        // On normal HashMap<T, V> the keySet is instantiated on first call and then updated thereafter
        // so returning this would open the caller up to thread safety issues, hence ConcurrentHashMap
        return _subscribers.keySet();
    }
}
