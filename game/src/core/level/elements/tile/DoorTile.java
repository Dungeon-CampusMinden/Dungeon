package core.level.elements.tile;

import core.Entity;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

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

    private final String closedTexturePath;

    private boolean open;

    /**
     * Creates a new Tile.
     *
     * <p>The door will be open.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile
     * @param level The level this Tile belongs to
     */
    public DoorTile(
            String texturePath, Coordinate globalPosition, DesignLabel designLabel, ILevel level) {
        super(texturePath, globalPosition, designLabel, level);
        String[] splitPath = texturePath.split("\\.");
        closedTexturePath = splitPath[0] + "_closed." + splitPath[1];
        levelElement = LevelElement.DOOR;
        open = true;
    }

    @Override
    public void onEntering(Entity element) {
        otherDoor.level.startTile(otherDoor.doorstep);
    }

    @Override
    public boolean isAccessible() {
        if (!open || (otherDoor != null && !otherDoor.isOpen())) return false;
        else return levelElement.value();
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

    /**
     * Open the door.
     *
     * <p>The player can use the door to enter the next room.
     */
    public void open() {
        open = true;
    }

    /**
     * Close the door.
     *
     * <p>The player can't use the door to enter the next room.
     */
    public void close() {
        open = false;
    }

    /**
     * Check if the door is open.
     *
     * @return true if the door is open, false if not.
     */
    public boolean isOpen() {
        return open;
    }

    @Override
    public String texturePath() {
        if (open && (otherDoor == null || otherDoor.isOpen())) return texturePath;
        else return closedTexturePath;
    }
}
