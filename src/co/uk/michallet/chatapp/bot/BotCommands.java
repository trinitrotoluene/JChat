package co.uk.michallet.chatapp.bot;

import co.uk.michallet.chatapp.common.AppThreadPool;
import co.uk.michallet.chatapp.common.HelpMenuBuilder;
import co.uk.michallet.chatapp.common.SDK.ChatEventFactory;
import co.uk.michallet.chatapp.common.commands.Command;
import co.uk.michallet.chatapp.common.commands.ExecutionResult;
import co.uk.michallet.chatapp.common.commands.IResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

public class BotCommands {
    private final Random _random;
    private final BotCommandContext _context;
    private final String[] _dadJokes = new String[] {
            "What does a clock do when it's hungry? It goes back four seconds!",
            "Did you hear about the bread factory burning down? They say the business is toast.",
            "When do doctors get angry? When they run out of patients.",
            "Parallel lines have so much in common. It’s a shame they’ll never meet.",
            "Why don't skeletons ride roller coasters? They don't have the stomach for it.",
            "What do you call someone with no nose? Nobody knows.",
            "R.I.P. boiled water. You will be mist.",
            "I wouldn't buy anything with velcro. It's a total rip-off.",
            "I knew I shouldn't steal a mixer from work, but it was a whisk I was willing to take."
    };

    public BotCommands(BotCommandContext context) {
        _context = context;
        _random = new Random();
    }

    @Command("ECHO")
    public IResult echo(String[] args) {
        var message = String.join(" ", args);
        _context.getBot().sendEvent(ChatEventFactory.fromMessage("", message));

        return new ExecutionResult(true);
    }

    @Command("DADJOKE")
    public IResult dadJoke(String[] args) {
        var response = _dadJokes[_random.nextInt(_dadJokes.length)];
        _context.getBot().sendEvent(ChatEventFactory.fromMessage("", response));

        return new ExecutionResult(true);
    }

    @Command("HELP")
    public IResult help(String[] args) {
        var help = new HelpMenuBuilder()
                .setTitle("BOT HELP")
                .setDescription("I'm a chat bot!")
                .addItem("ECHO", "Echoes any provided text right back to you!")
                .addItem("DADJOKE", "Tells you a really, really funny joke.")
                .build();

        _context.getBot().sendEvent(ChatEventFactory.fromMessage("", help));

        return new ExecutionResult(true);
    }
}
