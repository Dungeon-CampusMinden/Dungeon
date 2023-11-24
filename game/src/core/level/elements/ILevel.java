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
import core.level.elements.tile.*;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.List;
import java.util.Random;

public interface ILevel extends IndexedGraph<Tile> {

    Random RANDOM = new Random();

    /**
     * Mark a random tile as start
     */
    default void randomStart() {
        startTile(randomTile(LevelElement.FLOOR));
    }

    /**
     * Set the start tile.
     *
     * @param start The start tile.
     */
    void startTile(Tile start);

    /**
     * Mark a random tile as end
     */
    default void randomEnd() {
        List<FloorTile> floorTiles = floorTiles();
        if (floorTiles.size() <= 1) {
            // not enough Tiles for startTile and ExitTile
            return;
        }
        int startTileIndex = floorTiles.indexOf((FloorTile) startTile());
        int index = RANDOM.nextInt(floorTiles.size() - 1);
        changeTileElementType(
                floorTiles.get(index < startTileIndex ? index : index + 1), LevelElement.EXIT);
    }

    /**
     * Add floor tile to level.
     *
     * @param tile new floor tile
     */
    void addFloorTile(FloorTile tile);

    /**
     * Add wall tile to level.
     *
     * @param tile new wall tile
     */
    void addWallTile(WallTile tile);

    /**
     * Add hole tile to level.
     *
     * @param tile new hole tile
     */
    void addHoleTile(HoleTile tile);

    /**
     * Add door tile to level.
     *
     * @param tile new door tile
     */
    void addDoorTile(DoorTile tile);

    /**
     * Add exit tile to level.
     *
     * @param tile new exit tile
     */
    void addExitTile(ExitTile tile);

    /**
     * Add skip tile to level.
     *
     * @param tile new skip tile
     */
    void addSkipTile(SkipTile tile);

    /**
     * Add unspecific tile to level.
     *
     * @param tile tile to add
     */
    void addTile(Tile tile);

    /**
     * Removes tile from the level
     *
     * @param tile Tile to be removed
     */
    void removeTile(Tile tile);

    /**
     * Returns List of all floor tiles of the level.
     *
     * @return list of floor tiles
     */
    List<FloorTile> floorTiles();

    /**
     * Returns List of all wall tiles of the level.
     *
     * @return list of wall tiles
     */
    List<WallTile> wallTiles();

    /**
     * Returns List of all hole tiles of the level.
     *
     * @return list of hole tiles
     */
    List<HoleTile> holeTiles();

    /**
     * Returns List of all door tiles of the level.
     *
     * @return list of door tiles
     */
    List<DoorTile> doorTiles();

    /**
     * Returns List of all exit tiles of the level.
     *
     * @return list of exit tiles
     */
    List<ExitTile> exitTiles();

    /**
     * Returns List of all skip tiles of the level.
     *
     * @return list of skip tiles
     */
    List<SkipTile> skipTiles();

    void addConnectionsToNeighbours(Tile checkTile);

    /**
     * F=Floor, W=Wall, E=Exit, S=Skip/Blank
     *
     * @return The level layout in String format
     */
    default String printLevel() {
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < layout().length; y++) {
            for (int x = 0; x < layout()[0].length; x++) {
                if (layout()[y][x].levelElement() == LevelElement.FLOOR) {
                    output.append("F");
                } else if (layout()[y][x].levelElement() == LevelElement.WALL) {
                    output.append("W");
                } else if (layout()[y][x].levelElement() == LevelElement.EXIT) {
                    output.append("E");
                } else {
                    output.append("S");
                }
            }
            output.append("\n");
        }
        return output.toString();
    }

    /**
     * Change the type of tile (including changing texture)
     *
     * @param tile       The Tile you want to change
     * @param changeInto The LevelElement to change the Tile into.
     */
    default void changeTileElementType(Tile tile, LevelElement changeInto) {
        ILevel level = tile.level();
        if (level == null) {
            return;
        }
        level.removeTile(tile);
        Tile newTile =
                TileFactory.createTile(
                        TileTextureFactory.findTexturePath(tile, layout(), changeInto),
                        tile.coordinate(),
                        changeInto,
                        tile.designLabel());
        level.layout()[tile.coordinate().y][tile.coordinate().x] = newTile;
        level.addTile(newTile);
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    default Tile randomTile(LevelElement elementType) {
        return switch (elementType) {
            case SKIP -> skipTiles().size() > 0
                    ? skipTiles().get(RANDOM.nextInt(skipTiles().size()))
                    : null;
            case FLOOR -> floorTiles().size() > 0
                    ? floorTiles().get(RANDOM.nextInt(floorTiles().size()))
                    : null;
            case WALL -> wallTiles().size() > 0
                    ? wallTiles().get(RANDOM.nextInt(wallTiles().size()))
                    : null;
            case HOLE -> holeTiles().size() > 0
                    ? holeTiles().get(RANDOM.nextInt(holeTiles().size()))
                    : null;
            case EXIT -> exitTiles().size() > 0
                    ? exitTiles().get(RANDOM.nextInt(exitTiles().size()))
                    : null;
            case DOOR -> doorTiles().size() > 0
                    ? doorTiles().get(RANDOM.nextInt(doorTiles().size()))
                    : null;
        };
    }

    /**
     * Set the function that should be executed if this level is loaded as the current level for the
     * first time.
     *
     * @param function The function to be executed when the level is loaded for the first time.
     */
    void onFirstLoad(IVoidFunction function);

    /**
     * Notify the level that it has been loaded as the current level.
     *
     * <p>This function should check if the level was loaded for the first time and then execute the
     * registered function from {@link #onFirstLoad(IVoidFunction)}.
     */
    void onLoad();

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
     * @param end   End tile
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

    /**
     * @return The layout of the level
     */
    Tile[][] layout();

    /**
     * Get the tile at the given position.
     *
     * @param coordinate Position form where to get the tile.
     * @return The tile on that coordinate. null if there is no Tile or the Coordinate is out of
     * bound
     */
    default Tile tileAt(Coordinate coordinate) {
        try {
            return layout()[coordinate.y][coordinate.x];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Get the tile at the given position.
     *
     * <p>Will use {@link Point#toCoordinate} to convert the point into a coordinate.
     *
     * @param point Position form where to get the tile.
     * @return The tile on that point. null if there is no Tile or the Coordinate is out of bound
     */
    default Tile tileAt(Point point) {
        return tileAt(point.toCoordinate());
    }

    /**
     * @return a random Tile in the Level
     */
    default Tile randomTile() {
        return layout()[RANDOM.nextInt(layout().length)][RANDOM.nextInt(layout()[0].length)];
    }

    /**
     * Get the end tile.
     *
     * @return The end tile.
     */
    Tile endTile();

    /**
     * Get the start tile.
     *
     * @return The start tile.
     */
    Tile startTile();

    /**
     * Returns the tile the given entity is standing on.
     *
     * @param entity entity to check for.
     * @return tile at the coordinate of the entity
     */
    default Tile tileAtEntity(Entity entity) {
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        return tileAt(pc.position().toCoordinate());
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @return Position of the Tile as Point
     */
    default Point randomTilePoint() {
        return randomTile().position();
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @param elementTyp Type of the Tile
     * @return Position of the Tile as Point
     */
    default Point randomTilePoint(LevelElement elementTyp) {
        return randomTile(elementTyp).position();
    }
}
