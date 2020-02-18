package co.uk.michallet.chatapp.bot;

import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.commands.Command;
import co.uk.michallet.chatapp.common.commands.ExecutionResult;
import co.uk.michallet.chatapp.common.commands.IResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BotCommands {
    private final BotCommandContext _context;

    public BotCommands(BotCommandContext context) {
        _context = context;
    }

    @Command("ECHO")
    public IResult echo(String[] args) {
        var message = String.join(" ", args);
        _context.getBot().sendEvent(ChatEventFactory.fromMessage("", message));

        return new ExecutionResult(true);
    }

    @Command("DADJOKE")
    public IResult dadJoke(String[] args) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://icanhazdadjoke.com"))
                .header("Accept", "text/plain")
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            _context.getBot().sendEvent(ChatEventFactory.fromMessage("", response.body()));
            return new ExecutionResult(true);
        }
        catch (IOException | InterruptedException httpEx) {
            _context.getBot().sendEvent(ChatEventFactory.fromMessage("", "HTTP request failed"));
            return new ExecutionResult(false, "HTTP request failed");
        }
    }
}
