package co.uk.michallet.chatapp.bot.dod;

/**
 * Instance specific metadata for Tiles used when pathfinding.
 */
public final class TileNode {
    private final Point2D _pos;
    private final Tile _tile;
    private Integer _distance;
    private TileNode _previous;

    /**
     * Create a new node with given position and associated tile.
     * @param pos Position of the tile in 2d space.
     * @param tile Associated tile metadata.
     */
    public TileNode(Point2D pos, Tile tile) {
        this._pos = pos;
        this._tile = tile;
        this._distance = -1;
    }

    /**
     * @return Get the position of the tile.
     */
    public Point2D getPos() {
        return this._pos;
    }

    /**
     * @return Get the associated tile metadata.
     */
    public Tile getTile() {
        return this._tile;
    }

    /**
     * @return Get the computed distance from the origin.
     */
    public Integer getDistance () {
        return this._distance;
    }

    /**
     * Set the value of the distance from the origin.
     * @param distance Value to set the distance to.
     */
    public void setDistance(Integer distance) {
        this._distance = distance;
    }

    /**
     * @return A pointer to the previous node in the graph.
     */
    public TileNode getPrevious() {
        return this._previous;
    }

    /**
     * Set the value of the previous node.
     * @param node Pointer to the node to set.
     */
    public void setPrevious(TileNode node) {
        this._previous = node;
    }
}
