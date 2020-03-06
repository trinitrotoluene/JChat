package co.uk.michallet.chatapp.client;

import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.commands.Command;
import co.uk.michallet.chatapp.common.commands.ExecutionResult;
import co.uk.michallet.chatapp.common.commands.IResult;

import java.util.Arrays;

public class ClientCommands {
    private ClientCommandContext _context;

    public ClientCommands(ClientCommandContext context) {
        _context = context;
    }

    @Command("EXIT")
    public IResult exit(String[] args) {
        _context.getClient().cancel();

        return new ExecutionResult(true);
    }

    @Command("NICK")
    public IResult rename(String[] args) {
        if (args.length != 1) {
            return new ExecutionResult(false, "only one parameter accepted");
        }
        var renameEvent = ChatEventFactory.fromNameChange("", args[0]);
        _context.getClient().sendEvent(renameEvent);
        return new ExecutionResult(true);
    }

    @Command("DM")
    public IResult dm(String[] args) {
        if (args.length < 2) {
            return new ExecutionResult(false, "You must specify at least 2 arguments");
        }
        var message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        var dmEvent = ChatEventFactory.fromDM("", args[0], message);
        _context.getClient().sendEvent(dmEvent);
        return new ExecutionResult(true);
    }
}
