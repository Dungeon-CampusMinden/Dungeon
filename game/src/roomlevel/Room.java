package roomlevel;

import java.util.LinkedHashSet;
import level.elements.Tile;
import level.elements.TileLevel;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.TileTextureFactory;

public class Room extends TileLevel implements IRoom {

    private LinkedHashSet<DoorTile> doors = new LinkedHashSet<>();

    public Room(Tile[][] layout) {
        super(layout);
    }

    public Room(LevelElement[][] layout, DesignLabel designLabel) {
        super(layout, designLabel);
    }

    public void addDoor(DoorTile door) {
        doors.add(door);
    }

    @Override
    public LinkedHashSet<DoorTile> getDoors() {
        return doors;
    }

    @Override
    public void removeExit() {
        getEndTile()
                .setLevelElement(
                        LevelElement.FLOOR,
                        TileTextureFactory.findTexturePath(
                                getEndTile(), getLayout(), LevelElement.FLOOR));
    }
}
