package co.uk.michallet.chatapp.bot.dod;

/**
 * Implementation of the LOOK command from spec.
 */
public final class LookCommand implements GameCommand {
    private static LookCommand _instance = new LookCommand();

    /**
     * @return An instance of this singleton.
     */
    public static LookCommand getInstance() {
        return _instance;
    }

    private LookCommand() {
    }

    /**
     * Execute the LOOK command.
     * @param context The context the command should be executed in.
     */
    @Override
    public void execute(DoDCommandContext context) {
        int startX = context.getSender().getPos().getX() - 2;
        int startY = context.getSender().getPos().getY() - 2;

        var sb = new StringBuilder();
        for (int y = startY; y < context.getSender().getPos().getY() + 3; y++) {
            for (int x = startX; x < context.getSender().getPos().getX() + 3; x++) {
                var tile = context.getMap().getTileAt(new Point2D(x, y));
                sb.append(tile.render()).append(" ");
            }
            sb.append(System.lineSeparator());
        }

        context.getSender().sendMessage(sb.toString());
    }
}
