package co.uk.michallet.chatapp.bot.dod;

/**
 * Implementation of the GOLD command from spec.
 */
public final class GoldCommand implements GameCommand {
    private static GoldCommand _instance = new GoldCommand();

    /**
     * @return An instance of this singleton.
     */
    public static GoldCommand getInstance() {
        return _instance;
    }

    private GoldCommand() {
    }

    /**
     * Execute the GOLD command.
     * @param context The context the command should be executed in.
     */
    @Override
    public void execute(DoDCommandContext context) {
        context.getSender().sendMessage("Gold owned: %s", context.getSender().getGold());
    }
}
