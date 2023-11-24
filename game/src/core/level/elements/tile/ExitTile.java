package core.level.elements.tile;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

/**
 * Represents the exit in the dungeon.
 *
 * <p>If the exit is entered by the player, the {@link core.systems.LevelSystem} will generate a new
 * level.
 */
public class ExitTile extends Tile {

    /**
     * Creates a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile
     */
    public ExitTile(
            final String texturePath,
            final Coordinate globalPosition,
            final DesignLabel designLabel) {
        super(texturePath, globalPosition, designLabel);
        levelElement = LevelElement.EXIT;
    }
}
