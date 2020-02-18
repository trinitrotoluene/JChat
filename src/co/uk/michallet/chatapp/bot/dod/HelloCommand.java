package co.uk.michallet.chatapp.bot.dod;

/**
 * Implementation of the HELLO command from spec.
 */
public final class HelloCommand implements GameCommand {
    private static HelloCommand _instance = new HelloCommand();

    /**
     * @return An instance of this singleton.
     */
    public static HelloCommand getInstance() {
        return _instance;
    }

    private HelloCommand() {
    }

    /**
     * Execute the HELLO command.
     * @param context The context the command should be executed in.
     */
    @Override
    public void execute(DoDCommandContext context) {
        context.getSender().sendMessage("Gold to win: %s", context.getMap().getWinGold());
    }
}
