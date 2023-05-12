package api.level.elements.tile;

import api.Entity;
import api.level.Tile;
import api.level.elements.ILevel;
import api.level.utils.Coordinate;
import api.level.utils.DesignLabel;
import api.level.utils.LevelElement;

public class HoleTile extends Tile {

    /**
     * Creates a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile
     * @param level The level this Tile belongs to
     */
    public HoleTile(
            String texturePath, Coordinate globalPosition, DesignLabel designLabel, ILevel level) {
        super(texturePath, globalPosition, designLabel, level);
        levelElement = LevelElement.HOLE;
    }

    @Override
    public void onEntering(Entity element) {}

    @Override
    public boolean isAccessible() {
        return levelElement.getValue();
    }
}
