package co.uk.michallet.chatapp.bot.dod;

import java.util.*;

/**
 * A breadth-first pathfinding implementation.
 */
public final class GameMapGraph {
    private final GameMap _map;
    private final Set<TileNode> _nodes;
    private final Map<Point2D, Set<Point2D>> _edges;

    /**
     * Creates a graph from a given {@link GameMap}
     * @param map Map to extract nodes from.
     */
    public GameMapGraph(GameMap map) {
        this._map = map;
        this._nodes = new HashSet<>();
        this._edges = new HashMap<>();

        for (int x = 0; x < this._map.getTiles().length; x++) {
            for (int y = 0; y < this._map.getTiles()[0].length; y++) {
                var point = new Point2D(x, y);
                var currentTile = this._map.getTileAt(point);
                // If this tile can't be moved into, then it's not a node.
                if (!currentTile.isLegalMove())
                    continue;

                this._nodes.add(new TileNode(point, currentTile));
                // Link this node's position to any neighbours we can move into.
                addAllValidEdges(point);
            }
        }
    }

    /**
     * Get the node represented by a point in the graph.
     * @param point Point to search by.
     * @return Node represented by the point.
     */
    public TileNode getNode(Point2D point) {
        return this._nodes.stream()
                .filter(n -> n.getPos().equals(point))
                .findFirst()
                .orElseThrow();
    }

    private void addAllValidEdges(Point2D point) {
        var positions = new ArrayList<Point2D>();
        // Add all 4 directions, we'll validate them later.
        positions.add(new Point2D(point.getX(), point.getY() - 1));
        positions.add(new Point2D(point.getX(), point.getY() + 1));
        positions.add(new Point2D(point.getX() - 1, point.getY()));
        positions.add(new Point2D(point.getX() + 1, point.getY()));

        var set = new HashSet<Point2D>();
        for (var pos : positions) {
            // Get the tile that this point represents.
            var node = this._map.getTileAt(pos);
            // If it's legal, it's an edge.
            if (node.isLegalMove()) {
                set.add(pos);
            }
        }
        // Add the edges for this node.
        this._edges.put(point, set);
    }

    /**
     * Searches the graph for a path between two points.
     * @param start Location of the starting position.
     * @param end Location of the destination.
     * @return The final node of the path.
     */
    public TileNode findPathBetween(Point2D start, Point2D end) {
        // The reference for this algorithm is information taken from https://en.wikipedia.org/wiki/Pathfinding
        var frontier = new LinkedList<TileNode>();
        var startNode = this.getNode(start);
        startNode.setDistance(0);
        frontier.push(startNode);
        // While we have unchecked elements.
        while (frontier.size() > 0) {
            // Remove an element from the list.
            var current = frontier.pop();
            // Iterate over all valid neighbours (precomputed in the ctor)
            for (var pos : this._edges.get(current.getPos())) {
                // Get the node associated with this location.
                var next = this.getNode(pos);
                // If distance > 0 we've visited it before.
                if (next.getDistance() < 0)
                {
                    // Add it to the list to be checked.
                    frontier.add(next);
                    // Set distance + 1
                    next.setDistance(current.getDistance() + 1);
                    // Add a reference to the previous element so we can find our way back later.
                    next.setPrevious(current);
                }
            }
        }
        // Return the target node, if getPrevious isn't null, a valid path was found!
        return this.getNode(end);
    }

    /**
     * Convenience method to retrace all previous nodes from a given node and push them into a list.
     * @param node Node to begin retracing from.
     * @return A list of points that make up the path.
     */
    public static LinkedList<Point2D> convertNodeToPath(TileNode node) {
        var list = new LinkedList<Point2D>();

        while (node.getPrevious() != null) {
            list.push(node.getPos());
            node = node.getPrevious();
        }

        return list;
    }
}
