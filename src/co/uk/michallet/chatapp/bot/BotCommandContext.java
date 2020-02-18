package co.uk.michallet.chatapp.bot;

import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;
import co.uk.michallet.chatapp.common.commands.CommandContext;

public class BotCommandContext extends CommandContext {
    private final ChatBot _bot;

    public BotCommandContext(ChatBot bot, IConfiguration config, ILogger logger) {
        super(config, logger);
        _bot = bot;
    }

    public ChatBot getBot() {
        return _bot;
    }
}
