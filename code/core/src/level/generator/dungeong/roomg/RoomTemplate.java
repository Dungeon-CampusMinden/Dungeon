package level.generator.dungeong.roomg;

import java.util.ArrayList;
import java.util.List;
import level.elements.room.Room;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;

/**
 * A RoomTemplate is a blueprint for a room.
 *
 * @author Andre Matutat
 */
public class RoomTemplate {
    private LevelElement[][] layout;
    private DesignLabel design;
    private Coordinate localRef;
    private List<Coordinate> doors;

    /**
     * A RoomTemplate can be used to create a room.
     *
     * @param layout The layout of the room.
     * @param label The DesignLabel of the room.
     */
    public RoomTemplate(LevelElement[][] layout, DesignLabel label, Coordinate localRef) {
        setLayout(layout);
        this.design = label;
        this.localRef = localRef;
    }

    /**
     * Copy a roomTemplate.
     *
     * @param r Original template.
     */
    public RoomTemplate(RoomTemplate r) {
        layout = r.getLayout();
        design = r.getDesign();
        localRef = r.getLocalRef();
        doors = new ArrayList<>();
        for (Coordinate c : r.getDoors()) doors.add(c);
    }

    private void calculateDoors() {
        if (doors == null) {
            doors = new ArrayList<>();
            for (int x = 0; x < layout[0].length; x++)
                for (int y = 0; y < layout.length; y++)
                    if (layout[y][x] == LevelElement.DOOR) doors.add(new Coordinate(x, y));
        }
    }

    /**
     * @return A new template with a 90degree rotated layout.
     */
    public RoomTemplate rotateTemplate() {
        LevelElement[][] originalLayout = getLayout();
        int mSize = originalLayout.length;
        int nSize = originalLayout[0].length;
        LevelElement[][] rotatedLayout = new LevelElement[nSize][mSize];
        for (int row = 0; row < mSize; row++)
            for (int col = 0; col < nSize; col++)
                rotatedLayout[col][mSize - 1 - row] = originalLayout[row][col];

        return new RoomTemplate(rotatedLayout, getDesign(), getLocalRef());
    }

    /**
     * @return All rotated templates (0,90,180,270):
     */
    public List<RoomTemplate> getAllRotations() {
        List<RoomTemplate> allRotations = new ArrayList<>();
        allRotations.add(this);
        RoomTemplate r90 = rotateTemplate();
        allRotations.add(r90);
        RoomTemplate r180 = r90.rotateTemplate();
        allRotations.add(r180);
        allRotations.add(r180.rotateTemplate());
        return allRotations;
    }

    /**
     * Replace all placeholder with the replacements in the list.
     *
     * @param globalRef Where is this localRef positioned in the global system?
     * @param design Design of the room.
     * @return the created room
     */
    public Room convertToRoom(Coordinate globalRef, DesignLabel design) {
        int layoutHeight = layout.length;
        int layoutWidth = layout[0].length;
        LevelElement[][] roomLayout = new LevelElement[layoutHeight][layoutWidth];

        // copy layout
        for (int y = 0; y < layoutHeight; y++)
            for (int x = 0; x < layoutWidth; x++) roomLayout[y][x] = layout[y][x];

        // replace unplaced doors with walls
        for (int y = 0; y < layoutHeight; y++)
            for (int x = 0; x < layoutWidth; x++)
                if (roomLayout[y][x] == LevelElement.DOOR) roomLayout[y][x] = LevelElement.WALL;
        return new Room(roomLayout, design, localRef, globalRef);
    }

    public Room convertToRoom(Coordinate globalRef) {
        if (design == DesignLabel.ALL) return convertToRoom(globalRef, DesignLabel.randomDesign());
        else return convertToRoom(globalRef, design);
    }

    public LevelElement[][] getLayout() {
        // copy of the layout (IMPORTANT)
        return copyLayout(layout);
    }

    public void setLayout(LevelElement[][] layout) {
        this.layout = copyLayout(layout);
    }

    private LevelElement[][] copyLayout(LevelElement[][] toCopy) {
        LevelElement[][] copy = new LevelElement[toCopy.length][toCopy[0].length];
        for (int y = 0; y < toCopy.length; y++)
            for (int x = 0; x < toCopy[0].length; x++) {
                copy[y][x] = toCopy[y][x];
            }
        return copy;
    }

    public List<Coordinate> getDoors() {
        if (doors == null) calculateDoors();
        return doors;
    }

    public DesignLabel getDesign() {
        return design;
    }

    public Coordinate getLocalRef() {
        return new Coordinate(localRef.x, localRef.y);
    }

    public void setLocalRef(Coordinate c) {
        localRef = c;
    }

    public void useDoor(Coordinate c) {
        layout[c.y][c.x] = LevelElement.PLACED_DOOR;
    }
}
