package co.uk.michallet.chatapp.bot.dod;

import java.util.Arrays;
import java.util.Random;

/**
 * Static class containing helper methods for operations on entities working with {@link GameMap}s.
 */
public final class MapUtil {
    private MapUtil(){
    }

    /**
     * Generates a random point within a {@link GameMap} which a {@link PlayerBase} can start the game in.
     * @param map Map to search for points within.
     * @param otherStartingPoints Optional array of points which have already been selected.
     * @return A valid position for a player to start the game in.
     */
    public static Point2D getLegalStartingPoint(GameMap map, Point2D...otherStartingPoints) {
        var random = new Random();

        // Don't return until we've found something valid
        while (true) {
            // Get a random position on our map
            var x = random.nextInt(map.getTiles().length);
            var y = random.nextInt(map.getTiles()[0].length);
            var point = new Point2D(x, y);

            var tile = map.getTileAt(point);

            // Check whether moving into it would be legal and it's not equal to any of the other provided starting points.
            if (tile.isLegalMove() && (otherStartingPoints != null && Arrays.stream(otherStartingPoints)
                    .noneMatch(p -> p.equals(point)))) {
                // If it is, we've found our position
                return point;
            }
        }
    }
}
