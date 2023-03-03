package level.elements;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import level.elements.astar.TileHeuristic;
import level.elements.tile.Tile;

public interface IPathable extends IndexedGraph<Tile> {

    /**
     * For libGDX pathfinding algorithms
     *
     * @return nodeCount
     */
    int getNodeCount();

    /**
     * Starts the indexed A* pathfinding algorithm a returns a path
     *
     * @param start Start tile
     * @param end End tile
     * @return Generated path
     */
    default GraphPath<Tile> findPath(Tile start, Tile end) {
        GraphPath<Tile> path = new DefaultGraphPath<>();
        new IndexedAStarPathFinder<>(this).searchNodePath(start, end, getTileHeuristic(), path);
        return path;
    }

    @Override
    default int getIndex(Tile tile) {
        return tile.getIndex();
    }

    @Override
    default Array<Connection<Tile>> getConnections(Tile fromNode) {
        return fromNode.getConnections();
    }

    /**
     * @return the TileHeuristic for the Level
     */
    TileHeuristic getTileHeuristic();
}
