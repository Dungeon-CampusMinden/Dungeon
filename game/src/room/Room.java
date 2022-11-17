package room;

import java.util.LinkedHashSet;
import level.elements.TileLevel;
import level.elements.tile.DoorTile;
import level.elements.tile.Tile;
import level.tools.DesignLabel;
import level.tools.LevelElement;

/** A Level that can be used as a Room in a GraphLevel */
public class Room extends TileLevel implements IRoom {

    private LinkedHashSet<DoorTile> doors = new LinkedHashSet<>();

    public Room(Tile[][] layout) {
        super(layout);
    }

    public Room(LevelElement[][] layout, DesignLabel designLabel) {
        super(layout, designLabel);
    }

    /**
     * Add a door to the list
     *
     * @param door
     */
    public void addDoor(DoorTile door) {
        // oder: hier DoorTile erzeugen?
        doors.add(door);
    }

    @Override
    public LinkedHashSet<DoorTile> getDoors() {
        return doors;
    }
}
