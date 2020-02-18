package co.uk.michallet.chatapp.bot.dod;

/**
 * Implementation of the PICKUP command from spec.
 */
public final class QuitCommand implements GameCommand {
    private static QuitCommand _instance = new QuitCommand();

    /**
     * @return An instance of this singleton.
     */
    public static QuitCommand getInstance() {
        return _instance;
    }

    private QuitCommand() {
    }

    /**
     * Execute the QUIT command.
     * @param context The context the command should be executed in.
     */
    @Override
    public void execute(DoDCommandContext context) {
        var currentTile = context.getMap().getTileAt(context.getSender());

        if (currentTile.hasExit() && context.getMap().getWinGold() == context.getSender().getGold())
            context.getGame().setWon();

        context.getGame().setCompleted();
    }
}
