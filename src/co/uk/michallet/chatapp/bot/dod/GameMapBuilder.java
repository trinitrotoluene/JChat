package co.uk.michallet.chatapp.bot.dod;

/**
 * Builds a {@link GameMap}.
 */
public final class GameMapBuilder {
    private String _name;
    private Integer _winGold;
    private Tile[][] _tiles;

    public GameMapBuilder() {
    }

    /**
     * Set the name of the built map.
     * @param _name Name of the map.
     */
    public void withName(String _name) {
        this._name = _name;
    }

    /**
     * Set the winning gold requirement of the built map.
     * @param _winGold Gold required to exit and win the game.
     */
    public void withWinGold(Integer _winGold) {
        this._winGold = _winGold;
    }

    /**
     * Set the tiles making up the built map.
     * @param _tiles Tiles making up the map.
     */
    public void withTiles(Tile[][] _tiles) {
        this._tiles = _tiles;
    }

    /**
     * Build a map from stored properties.
     * @return An initialised {@link GameMap}
     */
    public GameMap build() {
        return new GameMap(this._name, this._winGold, this._tiles);
    }
}
