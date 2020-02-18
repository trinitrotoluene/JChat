package co.uk.michallet.chatapp.bot.dod;

import java.util.EnumSet;

/**
 * Wraps a bit field of {@link TileFlags} with helper methods to retrieve their value.
 */
public final class Tile {
    private EnumSet<TileFlags> _value;

    public Tile(EnumSet<TileFlags> value) {
        this._value = value;
    }

    /**
     * @return Whether the tile can be moved into.
     */
    public Boolean isEmpty() {
        return hasFlag(TileFlags.EMPTY);
    }

    /**
     * @return Whether there is a player in this tile.
     */
    public Boolean hasPlayer() {
        return hasFlag(TileFlags.PLAYER);
    }

    /**
     * @return Whether there is a bot in this tile.
     */
    public Boolean hasBot() {
        return hasFlag(TileFlags.BOT);
    }

    /**
     * @return Whether there is gold in this tile.
     */
    public Boolean hasGold() {
        return hasFlag(TileFlags.GOLD);
    }

    /**
     * @return Whether there is an exit in this tile.
     */
    public Boolean hasExit() {
        return hasFlag(TileFlags.EXIT);
    }

    /**
     * @return Whether there is a wall in this tile.
     */
    public Boolean hasWall() {
        return hasFlag(TileFlags.WALL);
    }

    /**
     * @return Whether this tile can be legally moved into by a player.
     */
    public Boolean isLegalMove() {
        return !this.hasWall();
    }

    /**
     * @param flag The value of the flag to set.
     */
    public void setFlag(TileFlags flag) {
        this._value.add(flag);
    }

    /**
     * @param flag The value of the flag to unset.
     */
    public void unsetFlag(TileFlags flag) {
        this._value.remove(flag);
    }

    /**
     * @return The map representation of this tile.
     */
    public String render() {
        if (this.hasBot())
            return "B";
        if (this.hasPlayer())
            return "P";
        if (this.hasGold())
            return "G";
        if (this.hasExit())
            return "E";
        if (this.isEmpty())
            return ".";
        if (this.hasWall())
            return "#";

        return "\0";
    }

    /**
     * @return The human-readable text representation of all flags present on the Tile.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("|");
        for (TileFlags flag : TileFlags.values()) {
            if (this.hasFlag(flag))
                sb.append(flag.name())
                        .append("|");
        }

        return sb.toString();
    }

    /**
     * @param flag The flag to test against the internal value.
     * @return Whether the Tile contains this flag.
     */
    private Boolean hasFlag(TileFlags flag) {
        return this._value.contains(flag);
    }
}
