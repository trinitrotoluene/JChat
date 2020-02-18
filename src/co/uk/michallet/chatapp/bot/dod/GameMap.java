package co.uk.michallet.chatapp.bot.dod;

import java.util.EnumSet;

/**
 * Contains the details of a parsed game map.
 * @see MapLoader
 */
public final class GameMap {
    private final String _name;
    private final Integer _winGold;
    private final Tile[][] _tiles;

    /**
     * Construct a new map from parsed details.
     * @param name Name of the map.
     * @param winGold Gold required to exit and win the game.
     * @param tiles Map tiles.
     */
    GameMap(String name, Integer winGold, Tile[][] tiles) {
        _name = name;
        _winGold = winGold;
        _tiles = tiles;
    }

    /**
     * @return The name of the map.
     */
    public String getName() {
        return this._name;
    }

    /**
     * @return The winning quantity of gold.
     */
    public Integer getWinGold() {
        return this._winGold;
    }

    /**
     * @return The tiles of the map.
     */
    public Tile[][] getTiles() {
        return this._tiles;
    }

    /**
     * @return A bounds-checked and OOB-tolerant retrieval of a map tile from a specific {@link Point2D}.
     * @param point Point to query the map at.
     */
    public Tile getTileAt(Point2D point) {
        if (point.getX() < 0 || point.getX() >= getTiles().length) {
            return new Tile(EnumSet.of(TileFlags.WALL));
        }
        else if (point.getY() < 0 || point.getY() >= getTiles()[0].length) {
            return new Tile(EnumSet.of(TileFlags.WALL));
        }

        return getTiles()[point.getX()][point.getY()];
    }

    /**
     * Helper method to simplify retrieving a player's current {@link Tile}
     * @return The tile a player is currently located in.
     * @param player Player to retrieve the current tile of.
     */
    public Tile getTileAt(PlayerBase player) {
        return getTileAt(player.getPos());
    }
}
