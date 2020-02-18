package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.commands.CommandContext;

public class ServerCommandContext extends CommandContext {
    private final ConcurrentMessageBus _messageBus;
    private final ChatServer _server;

    public ServerCommandContext(ChatServer server, IConfiguration config, ILogger logger, ConcurrentMessageBus messageBus) {
        super(config, logger);
        _server = server;
        _messageBus = messageBus;
    }

    public ChatServer getServer() {
        return _server;
    }

    public ConcurrentMessageBus getMessageBus() {
        return _messageBus;
    }
}
