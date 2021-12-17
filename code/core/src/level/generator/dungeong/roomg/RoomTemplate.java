package level.generator.dungeong.roomg;

import level.elements.Room;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import tools.Point;

import java.util.ArrayList;
import java.util.List;

/** @author Andre Matutat */
public class RoomTemplate {
    private LevelElement[][] layout;
    private DesignLabel design;
    private Point localRef;

    /**
     * A RoomTemplate can be used to create a room.
     *
     * @param layout the layout of the room
     * @param label the DesignLabel of the room
     */
    public RoomTemplate(LevelElement[][] layout, DesignLabel label, Point localRef) {
        setLayout(layout);
        this.design = label;
        this.localRef = localRef;
    }

    /**
     * copy a room
     *
     * @param r original room
     */
    public RoomTemplate(RoomTemplate r) {
        layout = new LevelElement[r.getLayout()[0].length][r.getLayout().length];
        for (int y = 0; y < layout.length; y++)
            for (int x = 0; x < layout[0].length; x++) layout[x][y] = r.getLayout()[x][y];
        design = r.getDesign();
    }

    /**
     * Replace all placeholder with the replacements in the list
     *
     * @param replacements list of replacements
     * @return the created room
     */
    public Room replace(final List<Replacement> replacements, Point globalRef) {
        int layoutHeight = layout.length;
        int layoutWidth = layout[0].length;
        LevelElement[][] roomLayout = new LevelElement[layoutHeight][layoutWidth];

        // copy layout
        for (int y = 0; y < layoutHeight; y++)
            for (int x = 0; x < layoutWidth; x++) roomLayout[y][x] = layout[y][x];

        // remove all replacements that are too big
        List<Replacement> replacementList = new ArrayList<>(replacements);
        for (Replacement r : replacements) {
            if (r.getLayout()[0].length <= layoutWidth && r.getLayout().length <= layoutHeight)
                replacementList.add(r);
        }

        // replace with replacements
        boolean changes;
        do {
            changes = false;
            for (Replacement r : replacementList) {
                int rHeight = r.getLayout().length;
                int rWidth = r.getLayout()[0].length;
                for (int y = 0; y < layoutHeight - rHeight; y++)
                    for (int x = 0; x < layoutWidth - rWidth; x++)
                        if (roomLayout[y][x] == LevelElement.WILDCARD
                                && placeIn(roomLayout, r, x, y)) changes = true;
            }
        } while (changes);

        // replace all placeholder that are left with floor
        for (int y = 0; y < layoutHeight; y++)
            for (int x = 0; x < layoutWidth; x++)
                if (roomLayout[y][x] == LevelElement.WILDCARD)
                    roomLayout[y][x] = LevelElement.FLOOR;

        return new Room(roomLayout, getDesign(), localRef, globalRef);
    }

    /**
     * Replace a specific spot in the layout
     *
     * @param layout layout to replace in
     * @param r the replacement
     * @param xCor place the left upper corner of the replacement on this x
     * @param yCor place the left upper corner of the replacement on this y
     * @return if replacement was done
     */
    private boolean placeIn(
            final LevelElement[][] layout, final Replacement r, int xCor, int yCor) {
        if (!canReplaceIn(layout, r, xCor, yCor)) return false;
        else {
            LevelElement[][] rlayout = r.getLayout();
            for (int y = yCor; y < yCor + rlayout.length; y++)
                for (int x = xCor; x < xCor + rlayout[0].length; x++) {
                    if (rlayout[y - yCor][x - xCor] != LevelElement.SKIP)
                        layout[y][x] = rlayout[y - yCor][x - xCor];
                }
            return true;
        }
    }

    /**
     * Check if a replacement fit in a specific spot on the layout
     *
     * @param layout layout to replace in
     * @param r the replacement
     * @param xCor place the left upper corner of the replacement on this x
     * @param yCor place the left upper corner of the replacement on this y
     * @return if replacement can be done
     */
    private boolean canReplaceIn(LevelElement[][] layout, final Replacement r, int xCor, int yCor) {
        LevelElement[][] rlayout = r.getLayout();
        for (int y = yCor; y < yCor + rlayout.length; y++)
            for (int x = xCor; x < xCor + rlayout[0].length; x++) {
                if (rlayout[y - yCor][x - xCor] != LevelElement.SKIP
                        && layout[y][x] != LevelElement.WILDCARD) return false;
            }
        return true;
    }

    public LevelElement[][] getLayout() {
        // copy of the layout
        return copyLayout(layout);
    }

    public void setLayout(LevelElement[][] layout) {
        this.layout = copyLayout(layout);
    }

    private LevelElement[][] copyLayout(LevelElement[][] toCopy) {
        LevelElement[][] copy = new LevelElement[layout.length][layout[0].length];
        for (int y = 0; y < toCopy.length; y++)
            for (int x = 0; x < toCopy[0].length; x++) {
                copy[y][x] = toCopy[y][x];
            }
        return copy;
    }

    public DesignLabel getDesign() {
        return design;
    }
}
