package level.elements.tile;

import level.elements.ILevel;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;

public class TileFactory {

    public static Tile createTile(
            String texturePath,
            Coordinate coordinate,
            LevelElement elementType,
            DesignLabel designLabel,
            ILevel level) {
        switch (elementType) {
            case FLOOR -> {
                FloorTile tile = new FloorTile(texturePath, coordinate, designLabel, level);
                level.addFloorTile(tile);
                return tile;
            }
            case WALL -> {
                WallTile tile = new WallTile(texturePath, coordinate, designLabel, level);
                level.addWallTile(tile);
                return tile;
            }
            case HOLE -> {
                HoleTile tile = new HoleTile(texturePath, coordinate, designLabel, level);
                level.addHoleTile(tile);
                return tile;
            }
            case DOOR -> {
                DoorTile tile = new DoorTile(texturePath, coordinate, designLabel, level);
                level.addDoorTile(tile);
                return tile;
            }
            case EXIT -> {
                ExitTile tile = new ExitTile(texturePath, coordinate, designLabel, level);
                level.addExitTile(tile);
                return tile;
            }
        }
        SkipTile tile = new SkipTile(texturePath, coordinate, designLabel, level);
        level.addSkipTile(tile);
        return tile;
    }
}
