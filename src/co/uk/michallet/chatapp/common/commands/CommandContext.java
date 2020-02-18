package co.uk.michallet.chatapp.common.commands;

import co.uk.michallet.chatapp.common.IConfiguration;
import co.uk.michallet.chatapp.common.ILogger;

public class CommandContext {
    private IConfiguration _config;
    private ILogger _logger;

    public CommandContext(IConfiguration config, ILogger logger) {
        _config = config;
        _logger = logger;
    }

    public IConfiguration getConfig() {
        return _config;
    }

    public ILogger getLogger() {
        return _logger;
    }
}
