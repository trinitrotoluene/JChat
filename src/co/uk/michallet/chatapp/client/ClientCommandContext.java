package co.uk.michallet.chatapp.client;

import co.uk.michallet.chatapp.bot.ChatBot;
import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.commands.CommandContext;

public class ClientCommandContext extends CommandContext {
    private final ChatClient _client;
    public ClientCommandContext(ChatClient client, IConfiguration config, ILogger logger) {
        super(config, logger);
        _client = client;
    }

    public ChatClient getClient() {
        return _client;
    }
}
