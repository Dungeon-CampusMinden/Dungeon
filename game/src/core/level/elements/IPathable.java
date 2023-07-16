package core.level.elements;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.astar.TileHeuristic;
import core.utils.Point;
import core.utils.components.MissingComponentException;

public interface IPathable extends IndexedGraph<Tile> {

    /**
     * For libGDX pathfinding algorithms
     *
     * @return nodeCount
     */
    int getNodeCount();

    /**
     * Starts the indexed A* pathfinding algorithm a returns a path.
     *
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
     *
     * @param start Start tile
     * @param end End tile
     * @return Generated path
     */
    default GraphPath<Tile> findPath(Tile start, Tile end) {
        if (!start.isAccessible())
            throw new IllegalArgumentException(
                    "Can not calculate Path because the start point is non-accessible.");
        if (!end.isAccessible())
            throw new IllegalArgumentException(
                    "Can not calculate Path because the end point is non-accessible.");
        GraphPath<Tile> path = new DefaultGraphPath<>();
        new IndexedAStarPathFinder<>(this).searchNodePath(start, end, tileHeuristic(), path);
        return path;
    }

    @Override
    default int getIndex(Tile tile) {
        return tile.index();
    }

    @Override
    default Array<Connection<Tile>> getConnections(Tile fromNode) {
        return fromNode.connections();
    }

    /**
     * @return the TileHeuristic for the Level
     */
    TileHeuristic tileHeuristic();

    /**
     * Get the Position of the given entity in the level.
     *
     * @param entity Entity to get the current position from (needs a {@link PositionComponent}
     * @return Position of the given entity.
     */
    default Point positionOf(Entity entity) {
        return entity.fetch(PositionComponent.class)
                .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class))
                .position();
    }
}
