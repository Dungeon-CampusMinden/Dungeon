package core.level.elements;

import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.position.Point;
import core.utils.position.Position;

import java.util.Random;

public interface ITileable extends IPathable {
    Random RANDOM = new Random();

    /**
     * @return The layout of the level
     */
    Tile[][] layout();

    /**
     * Get the tile at the given position.
     *
     * @param coordinate Position from where to get the tile.
     * @return The tile on that coordinate. null if there is no Tile or the Coordinate is out of
     *     bound
     */
    default Tile tileAt(Point coordinate) {
        try {
            return layout()[coordinate.y_i()][coordinate.x_i()];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Get the tile at the given position.
     *
     * <p>Will use {@link Position#point()} to convert the position into a coordinate.
     *
     * @param position Position form where to get the tile.
     * @return The tile on that position. null if there is no Tile or the Coordinate is out of bound
     */
    default Tile tileAt(Position position) {
        return tileAt(position.point());
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
        PositionComponent pc = (PositionComponent) entity.fetch(PositionComponent.class).get();
        return tileAt(pc.position().point());
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    Tile randomTile(LevelElement elementType);

    /**
     * Get the position of a random Tile as Position
     *
     * @return Position of the Tile as Position
     */
    default Position randomTilePoint() {
        return randomTile().position();
    }

    /**
     * Get the position of a random Tile as Position
     *
     * @param elementTyp Type of the Tile
     * @return Position of the Tile as Position
     */
    default Position randomTilePoint(LevelElement elementTyp) {
        return randomTile(elementTyp).position();
    }
}
