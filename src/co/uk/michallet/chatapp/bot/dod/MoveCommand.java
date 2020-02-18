package co.uk.michallet.chatapp.bot.dod;

/**
 * Implementation of the MOVE command from spec.
 */
public final class MoveCommand implements GameCommand {
    private static MoveCommand _instance = new MoveCommand();

    /**
     * @return An instance of this singleton.
     */
    public static MoveCommand getInstance() {
        return _instance;
    }

    private MoveCommand() {
    }

    /**
     * Execute the HELLO command.
     * @param context The context the command should be executed in.
     */
    @Override
    public void execute(CommandContext context) {
        var sender = context.getSender();
        if (context.getArgs().length != 1)
        {
            sender.sendMessage("FAIL");
            return;
        }

        var newX = sender.getPos().getX();
        var newY = sender.getPos().getY();
        switch (context.getArgs()[0].toLowerCase()) {
            case "n":
                newY -= 1;
                break;
            case "s":
                newY += 1;
                break;
            case "e":
                newX += 1;
                break;
            case "w":
                newX -= 1;
                break;
        }
        var newPos = new Point2D(newX, newY);
        var nextTile = context.getMap().getTileAt(newPos);
        var currentTile = context.getMap().getTileAt(sender);

        if (nextTile.isLegalMove()) {
            if (sender instanceof Player)
            {
                currentTile.unsetFlag(TileFlags.PLAYER);
                nextTile.setFlag(TileFlags.PLAYER);
            }
            else if (sender instanceof BotPlayer)
            {
                currentTile.unsetFlag(TileFlags.BOT);
                nextTile.setFlag(TileFlags.BOT);
            }

            sender.moveTo(newPos);
            sender.sendMessage("SUCCESS");

            if (nextTile.hasPlayer() && nextTile.hasBot()) {
                sender.sendMessage("The bot caught you!");
                context.getGame().setCompleted();
            }
        }
        else {
            sender.sendMessage("FAIL");
        }
    }
}
