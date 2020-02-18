package co.uk.michallet.chatapp.bot.dod;

/**
 * Base class used to abstract player interactions.
 */
public abstract class PlayerBase {
    private Point2D _pos;
    private Integer _currentGold;

    public PlayerBase(Point2D pos) {
        this._pos = pos;
        this._currentGold = 0;
    }

    public abstract String getNextCommand();

    public void moveTo(Point2D point) {
        this._pos = point;
    }

    public void sendMessage(String format, Object...args) {
        return;
    }

    public final Point2D getPos() {
        return this._pos;
    }

    public final int getGold() {
        return this._currentGold;
    }

    public final void addGold(int value) {
        this._currentGold += value;
    }
}
