package co.uk.michallet.chatapp.bot.dod;

import java.util.Objects;

/**
 * Represents a position in 2-dimensional space.
 */
public class Point2D {
    private int _x;
    private int _y;

    /**
     * Create a new point.
     * @param x The X co-ordinate of this point.
     * @param y The Y co-ordinate of this point.
     */
    public Point2D(int x, int y) {
        _x = x;
        _y = y;
    }

    public final Integer getX() {
        return _x;
    }

    public final Integer getY() {
        return _y;
    }

    public final void add(Point2D point) {
        this._x += point.getX();
        this._y += point.getY();
    }

    public final boolean equals(Point2D point) {
        return this._x == point.getX() && this._y == point.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this._x, this._y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point2D))
            return false;

        return ((Point2D) obj).equals(this);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", _x, _y);
    }
}
