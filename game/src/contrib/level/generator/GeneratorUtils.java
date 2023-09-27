package contrib.level.generator;

import contrib.level.generator.graphBased.levelGraph.Direction;

import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;

import java.util.Optional;

public class GeneratorUtils {

    /**
     * Get the LevelElement[][] for a Tile[][]
     *
     * @param tileLayout tile layout to parse
     * @return the parsed LevelElement layout.
     */
    public static LevelElement[][] parseToElementLayout(Tile[][] tileLayout) {
        int ySize = tileLayout.length;
        int xSize = tileLayout[0].length;
        LevelElement[][] elementLayout = new LevelElement[ySize][xSize];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                Tile tile = tileLayout[y][x];
                elementLayout[y][x] = tile.levelElement();
            }
        }
        return elementLayout;
    }

    /**
     * Get the direction where a door is placed
     *
     * @param level Level that contains the door.
     * @param door door-tile where to find the direction for
     * @return the direction of the door
     */
    public static Direction doorDirection(ILevel level, DoorTile door) {
        LevelElement[][] layout = parseToElementLayout(level.layout());
        if (TileTextureFactory.isTopWall(door.coordinate(), layout)) return Direction.NORTH;
        if (TileTextureFactory.isRightWall(door.coordinate(), layout)) return Direction.EAST;
        if (TileTextureFactory.isBottomWall(door.coordinate(), layout)) return Direction.SOUTH;
        return Direction.WEST;
    }

    /**
     * Get the DoorTile at the given direction if it exists.
     *
     * <p>Returns an empty Optional if no level is generated for this room, or if no door is at that
     * direction.
     *
     * @param direction Direction in which to find the door.
     * @return DoorTile in the room at the given direction.
     */
    public static Optional<DoorTile> doorAt(ILevel level, Direction direction) {
        for (DoorTile door : level.doorTiles())
            if (doorDirection(level, door) == direction) return Optional.of(door);
        return Optional.empty();
    }
}
