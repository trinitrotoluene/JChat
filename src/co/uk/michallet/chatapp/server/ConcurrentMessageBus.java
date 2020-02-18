package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.net.ChatEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMessageBus {
    private Map<String, ClientSession> _subscribers;

    public ConcurrentMessageBus() {
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
        return _subscribers.keySet();
    }
}
