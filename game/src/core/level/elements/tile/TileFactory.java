package core.level.elements.tile;

import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

public class TileFactory {

    private static Tile createTile(
            String texturePath,
            Coordinate coordinate,
            LevelElement elementType,
            DesignLabel designLabel,
            ILevel level) {
        return switch (elementType) {
            case FLOOR -> new FloorTile(texturePath, coordinate, designLabel, level);
            case WALL -> new WallTile(texturePath, coordinate, designLabel, level);
            case HOLE -> new HoleTile(texturePath, coordinate, designLabel, level);
            case DOOR -> new DoorTile(texturePath, coordinate, designLabel, level);
            case EXIT -> new ExitTile(texturePath, coordinate, designLabel, level);
            case SKIP -> new SkipTile(texturePath, coordinate, designLabel, level);
        };
    }

    /**
     * creates a new Tile which can then be added to the level
     *
     * @param texturePath the path to the texture
     * @param coordinate the position of the newly created Tile
     * @param elementType the type of the new Tile
     * @param designLabel the label for reasons
     * @return the newly created Tile
     */
    public static Tile createTile(
            String texturePath,
            Coordinate coordinate,
            LevelElement elementType,
            DesignLabel designLabel) {
        return createTile(texturePath, coordinate, elementType, designLabel, null);
    }
}
