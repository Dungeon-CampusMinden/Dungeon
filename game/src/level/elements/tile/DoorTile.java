package level.elements.tile;

import ecs.entities.Entity;
import level.elements.ILevel;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;

public class DoorTile extends Tile {

    public enum DoorColor {
        NONE,
        RED,
        BLUE,
        YELLOW,
        GREEN
    }

    private DoorTile otherDoor;
    private Tile doorstep;

    /**
     * Creates a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile
     * @param level The level this Tile belongs to
     */
    public DoorTile(
            String texturePath, Coordinate globalPosition, DesignLabel designLabel, ILevel level) {
        super(texturePath, globalPosition, designLabel, level);
        levelElement = LevelElement.DOOR;
    }

    @Override
    public void onEntering(Entity element) {
        otherDoor.level.setStartTile(otherDoor.doorstep);
    }

    @Override
    public boolean isAccessible() {
        return levelElement.getValue();
    }

    /**
     * Connects this door with its other side in another room.
     *
     * @param otherDoor Door that will be connected to this door
     */
    public void setOtherDoor(DoorTile otherDoor) {
        this.otherDoor = otherDoor;
    }

    /**
     * @return Door that is connected to this door
     */
    public DoorTile getOtherDoor() {
        return otherDoor;
    }

    /**
     * Sets Tile in front of the door.
     *
     * @param doorstep Tile in front of the door
     */
    public void setDoorstep(Tile doorstep) {
        this.doorstep = doorstep;
    }

    /**
     * @return Tile in front of the door
     */
    public Tile getDoorstep() {
        return doorstep;
    }

    /**
     * Sets the color of the door and changes texturePath accordingly.
     *
     * @param color New color of this door
     */
    public void setColor(DoorColor color) {
        if (texturePath != null) {
            StringBuilder textureBuilder = new StringBuilder(texturePath);
            int indexOfUnderscore = textureBuilder.indexOf("_");
            int indexOfDot = textureBuilder.indexOf(".");
            // TODO if (indexOfDot == -1) { error }
            if (indexOfUnderscore == -1) {
                if (color != DoorColor.NONE) {
                    textureBuilder.insert(indexOfDot, "_" + color.name().toLowerCase());
                }
            } else {
                if (color == DoorColor.NONE) {
                    textureBuilder.replace(indexOfUnderscore, indexOfDot, "");
                } else {
                    textureBuilder.replace(
                            indexOfUnderscore + 1, indexOfDot, color.name().toLowerCase());
                }
            }
            texturePath = textureBuilder.toString();
        } // TODO else { error }
    }
}
