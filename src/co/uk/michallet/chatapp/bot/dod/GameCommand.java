package co.uk.michallet.chatapp.bot.dod;

/**
 * A common interface implemented by all game commands.
 */
public interface GameCommand {
    void execute(DoDCommandContext context);
}
