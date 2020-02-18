package co.uk.michallet.chatapp.bot.dod;

/**
 * Implementation of a player who inputs commands through a Scanner, and overrides {@link #sendMessage(String, Object...)} to print responses to console.
 */
public final class Player extends PlayerBase {
    public Player(Point2D pos) {
        super(pos);
    }

    @Override
    public String getNextCommand() {
        return UI.getScanner().nextLine();
    }

    @Override
    public void sendMessage(String format, Object... args) {
        UI.write(format, args);
    }
}
