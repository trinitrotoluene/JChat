package co.uk.michallet.chatapp.bot.dod;

/**
 * Implementation of the PICKUP command from spec.
 */
public final class PickupCommand implements GameCommand {
    private static PickupCommand _instance = new PickupCommand();

    /**
     * @return An instance of this singleton.
     */
    public static PickupCommand getInstance() {
        return _instance;
    }

    private PickupCommand() {
    }

    /**
     * Execute the PICKUP command.
     * @param context The context the command should be executed in.
     */
    @Override
    public void execute(DoDCommandContext context) {
        var sender = context.getSender();

        var currentTile = context.getMap().getTiles()[sender.getPos().getX()][sender.getPos().getY()];
        if (currentTile.hasGold()) {
            currentTile.unsetFlag(TileFlags.GOLD);
            sender.addGold(1);
            sender.sendMessage("SUCCESS");
        }
        else {
            sender.sendMessage("FAIL");
        }
    }
}
