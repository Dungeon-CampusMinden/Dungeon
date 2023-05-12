package api.level.elements.tile;

import api.ecs.entities.Entity;
import api.level.elements.ILevel;
import api.level.tools.Coordinate;
import api.level.tools.DesignLabel;
import api.level.tools.LevelElement;

public class WallTile extends Tile {

    /**
     * Creates a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile
     * @param level The level this Tile belongs to
     */
    public WallTile(
            String texturePath, Coordinate globalPosition, DesignLabel designLabel, ILevel level) {
        super(texturePath, globalPosition, designLabel, level);
        levelElement = LevelElement.WALL;
    }

    @Override
    public void onEntering(Entity element) {}

    @Override
    public boolean isAccessible() {
        return levelElement.getValue();
    }
}
