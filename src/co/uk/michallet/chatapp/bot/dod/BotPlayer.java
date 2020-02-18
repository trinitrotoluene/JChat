package co.uk.michallet.chatapp.bot.dod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * An implementation of a player which no-ops on console responses and chases down other players.
 */
public final class BotPlayer extends PlayerBase {
    private final GameMap _map;

    public BotPlayer(Point2D pos, GameMap map) {
        super(pos);
        this._map = map;
    }

    private LinkedList<Point2D> _currentPath;

    @Override
    public String getNextCommand() {
        if (this._currentPath == null || this._currentPath.size() == 0)
            look();
        else {
            var nextMove = this._currentPath.pop();
            return getMoveCommandFromPoint(nextMove);
        }

        // Waste the turn. Bot doesn't need to go through the text-based command API when using look();
        return "";
    }

    /**
     * @param target Point that the current position should be compared to.
     * @return A valid MOVE command to return to the command system.
     */
    private String getMoveCommandFromPoint(Point2D target) {
        var current = this.getPos();

        if (target.getX() > current.getX())
            return "move e";
        else if (target.getX() < current.getX())
            return "move w";
        else if (target.getY() > current.getY())
            return "move s";
        else
            return "move n";
    }

    /**
     * Custom implementation of LOOK, since the bot can just fetch the 5x5 field without any indirection.
     * The bot still has the same field of view as the player.
     */
    private void look() {
        var graph = new GameMapGraph(this._map);
        var target = searchForPlayer(this.getPos());

        var node = graph.findPathBetween(this.getPos(), target);
        this._currentPath = GameMapGraph.convertNodeToPath(node);
    }

    /**
     * Private implementation of LOOK to avoid having to parse returned data which would create a weird dual-dependency.
     * And a parsing nightmare, honestly.
     * @param pos Position to look from.
     * @return Point that a player is located in or a random point in the 5x5 field.
     */
    private Point2D searchForPlayer(Point2D pos) {
        var possiblePoints = new ArrayList<Point2D>();
        for (int x = pos.getX() - 2; x < pos.getX() + 3; x++) {
            for (int y = pos.getY() - 2; y < pos.getY() + 3; y++) {
                var point = new Point2D(x, y);
                var tile = this._map.getTileAt(point);
                if (tile.hasPlayer())
                    return point;
                if (tile.isLegalMove() && !point.equals(pos))
                    possiblePoints.add(point);
            }
        }

        // Select a random valid point to path-find towards.
        var random = new Random();
        var index = random.nextInt(possiblePoints.size());
        return possiblePoints.get(index);
    }
}
