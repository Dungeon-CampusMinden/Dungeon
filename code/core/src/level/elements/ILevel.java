package level.elements;

import basiselements.Entity;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import java.util.Random;
import level.elements.astar.TileHeuristic;
import level.tools.Coordinate;
import level.tools.LevelElement;
import level.tools.TileTextureFactory;
import tools.Point;

public interface ILevel extends IndexedGraph<Tile> {
    Random RANDOM = new Random();

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

    /**
     * Checks if the passed entity is on the tile to the next level.
     *
     * @param entity entity to check for.
     * @return if the passed entity is on the tile to the next level
     */
    default boolean isOnEndTile(Entity entity) {
        return entity.getPosition().toCoordinate().equals(getEndTile().getCoordinate());
    }

    /**
     * Get a tile on the global position.
     *
     * @param globalPoint Position form where to get the tile.
     * @return The tile on that point. null if there is no Tile or the Coordinate is out of bound
     */
    default Tile getTileAt(Coordinate globalPoint) {
        try {
            return getLayout()[globalPoint.y][globalPoint.x];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * @return a random Tile in the Level
     */
    default Tile getRandomTile() {
        return getLayout()[RANDOM.nextInt(getLayout().length)][
                RANDOM.nextInt(getLayout()[0].length)];
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    default Tile getRandomTile(LevelElement elementType) {
        Tile randomTile = getRandomTile();
        if (randomTile.getLevelElement() == elementType) {
            return randomTile;
        } else {
            return getRandomTile(elementType);
        }
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @return Position of the Tile as Point
     */
    default Point getRandomTilePoint() {
        return getRandomTile().getCoordinate().toPoint();
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @param elementTyp Type of the Tile
     * @return Position of the Tile as Point
     */
    default Point getRandomTilePoint(LevelElement elementTyp) {
        return getRandomTile(elementTyp).getCoordinate().toPoint();
    }

    /**
     * @return The layout of the level
     */
    Tile[][] getLayout();

    /** Mark a random tile as start */
    default void setRandomStart() {
        setStartTile(getRandomTile(LevelElement.FLOOR));
    }

    /**
     * Get the start tile.
     *
     * @return The start tile.
     */
    Tile getStartTile();

    /**
     * Set the start tile.
     *
     * @param start The start tile.
     */
    void setStartTile(Tile start);

    /** Mark a random tile as end */
    default void setRandomEnd() {
        setEndTile(getRandomTile(LevelElement.FLOOR));
    }

    /**
     * Get the end tile.
     *
     * @return The end tile.
     */
    Tile getEndTile();

    /**
     * Set the end tile.
     *
     * @param end The end tile.
     */
    void setEndTile(Tile end);

    /**
     * Change the type of tile (including changing texture)
     *
     * @param tile The Tile you want to change
     * @param changeInto The LevelElement to change the Tile into.
     */
    default void changeTileElementType(Tile tile, LevelElement changeInto) {
        tile.setLevelElement(
                changeInto, TileTextureFactory.findTexturePath(tile, getLayout(), changeInto));
    }

    /**
     * For libGDX pathfinding algorithms
     *
     * @return nodeCount
     */
    int getNodeCount();

    /**
     * F=Floor, W=Wall, E=Exit, S=Skip/Blank
     *
     * @return The level layout in String format
     */
    default String printLevel() {
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < getLayout().length; y++) {
            for (int x = 0; x < getLayout()[0].length; x++) {
                if (getLayout()[y][x].getLevelElement() == LevelElement.FLOOR) {
                    output.append("F");
                } else if (getLayout()[y][x].getLevelElement() == LevelElement.WALL) {
                    output.append("W");
                } else if (getLayout()[y][x].getLevelElement() == LevelElement.EXIT) {
                    output.append("E");
                } else {
                    output.append("S");
                }
            }
            output.append("\n");
        }
        return output.toString();
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
