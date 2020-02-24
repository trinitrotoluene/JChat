package co.uk.michallet.chatapp.server;

import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.commands.Command;
import co.uk.michallet.chatapp.common.commands.ExecutionResult;
import co.uk.michallet.chatapp.common.commands.IResult;

public class ServerCommands {
    private ServerCommandContext _context;

    public ServerCommands(ServerCommandContext context) {
        _context = context;
    }

    /**
     * Sends a message to every client session connected to the server.
     */
    @Command("BROADCAST")
    public IResult broadcast(String[] args) {
        var message = String.join(" ", args);
        _context.getMessageBus().broadcast(ChatEventFactory.fromMessage("[SERVER]", message));
        return new ExecutionResult(true);
    }

    /**
     * Lists the name of every connected client.
     */
    @Command("LIST")
    public IResult list(String[] args) {
        var message = new StringBuilder();
        int size = _context.getMessageBus().getNames().size();
        message.append(size).append(" users online: ");
        message.append(String.join(", ", _context.getMessageBus().getNames()));

        _context.getLogger().info(message.toString());
        return new ExecutionResult(true);
    }

    /**
     * Shuts down the server.
     */
    @Command("EXIT")
    public IResult exit(String[] args) {
        _context.getServer().abort();
        return new ExecutionResult(true);
    }
}
