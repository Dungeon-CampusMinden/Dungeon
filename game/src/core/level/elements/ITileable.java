package core.level.elements;

import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;

import java.util.Random;

public interface ITileable extends IPathable {
    Random RANDOM = new Random();

    /**
     * @return The layout of the level
     */
    Tile[][] getLayout();

    /**
     * Get the tile at the given position.
     *
     * @param coordinate Position form where to get the tile.
     * @return The tile on that coordinate. null if there is no Tile or the Coordinate is out of
     *     bound
     */
    default Tile tileAt(Coordinate coordinate) {
        try {
            return getLayout()[coordinate.y][coordinate.x];
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
        return getLayout()[RANDOM.nextInt(getLayout().length)][
                RANDOM.nextInt(getLayout()[0].length)];
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
                (PositionComponent) entity.fetch(PositionComponent.class).get();
        return tileAt(pc.position().toCoordinate());
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    Tile randomTile(LevelElement elementType);

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
