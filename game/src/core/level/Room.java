package core.level;

import core.Entity;
import core.level.elements.tile.DoorTile;
import core.level.levelgraph.LevelNode;
import core.level.room.IRoom;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/** A Level that can be used as a Room in a GraphLevel */
public class Room extends TileLevel implements IRoom {

    private LinkedHashSet<DoorTile> doors = new LinkedHashSet<>();

    private ArrayList<Entity> elements = new ArrayList<>();

    private LevelNode levelNode;

    public Room(Tile[][] layout) {
        super(layout);
    }

    public Room(LevelElement[][] layout, DesignLabel designLabel, LevelNode node) {
        super(layout, designLabel);
        levelNode = node;
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

    public void addElement(Entity dungeonElement) {
        elements.add(dungeonElement);
    }

    public void removeElement(Entity dungeonElement) {
        elements.remove(dungeonElement);
    }

    public ArrayList<Entity> getElements() {
        return elements;
    }

    @Override
    public LinkedHashSet<DoorTile> getDoors() {
        return doors;
    }

    @Override
    public LevelNode getLevelNode() {
        return levelNode;
    }
}
