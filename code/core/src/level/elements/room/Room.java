package level.elements.room;

import java.util.Random;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.TileTextureFactory;

/**
 * A Room is a collection of tiles and has a position in the global system.
 *
 * @author Andre Matutat
 */
public class Room {
    private final DesignLabel design;
    private final Coordinate referencePointGlobal;
    private final Coordinate referencePointLocal;
    private Tile[][] layout;

    /**
     * @param layout List of tiles that defines the layout of the room.
     * @param label The design of the room.
     * @param referencePointLocal A point in the room to place it in the level.
     * @param referencePointGlobal The position of the local reference point in the global system.
     */
    public Room(
            LevelElement[][] layout,
            DesignLabel label,
            Coordinate referencePointLocal,
            Coordinate referencePointGlobal) {
        // choose random design label
        if (label == DesignLabel.ALL) {
            this.design =
                    DesignLabel.values()[new Random().nextInt(DesignLabel.values().length - 1)];
        } else this.design = label;

        this.layout = new Tile[layout.length][layout[0].length];
        this.referencePointGlobal = referencePointGlobal;
        this.referencePointLocal = referencePointLocal;
        convertLayout(layout);
    }

    /**
     * Converts the list of LevelElements in a list of tiles.
     *
     * @param toConvert The list to convert.
     */
    private void convertLayout(LevelElement[][] toConvert) {
        // calculate difference between global and local coordinates
        int difx = referencePointGlobal.x - referencePointLocal.x;
        int dify = referencePointGlobal.y - referencePointLocal.y;

        // concert LevelElement into Tile
        for (int y = 0; y < toConvert.length; y++)
            for (int x = 0; x < toConvert[0].length; x++) {
                Coordinate p = new Coordinate(x, y);
                String texture =
                        TileTextureFactory.findTexture(toConvert[y][x], design, toConvert, p);
                layout[y][x] =
                        new Tile(texture, new Coordinate(x + difx, y + dify), toConvert[y][x]);
            }
    }

    /** @return A copy of the layout. */
    public Tile[][] getLayout() {
        return copyLayout(layout);
    }

    /**
     * Set the layout of the room.
     *
     * @param layout The new layout.
     */
    public void setLayout(Tile[][] layout) {
        this.layout = copyLayout(layout);
    }

    /**
     * Copy's an layout.
     *
     * @param toCopy Layout to copy.
     * @return The copy.
     */
    private Tile[][] copyLayout(Tile[][] toCopy) {
        Tile[][] copy = new Tile[layout.length][layout[0].length];
        for (int y = 0; y < toCopy.length; y++)
            System.arraycopy(toCopy[y], 0, copy[y], 0, toCopy[0].length);
        return copy;
    }

    /** @return Random floor-tile in the room. */
    public Tile getRandomFloorTile() {
        Random r = new Random();
        Tile[][] layout = getLayout();
        int x, y;
        do {
            y = r.nextInt(layout.length);
            x = r.nextInt(layout[0].length);
        } while (layout[y][x].getLevelElement() != LevelElement.FLOOR);
        return layout[y][x];
    }

    /** @return Random wall-tile in the room. */
    public Tile getRandomWallTile() {
        Random r = new Random();
        Tile[][] layout = getLayout();
        int x, y;
        do {
            y = r.nextInt(layout.length);
            x = r.nextInt(layout[0].length);
        } while (layout[y][x].getLevelElement() != LevelElement.WALL);
        return layout[y][x];
    }

    public DesignLabel getDesign() {
        return design;
    }

    public Coordinate getReferencePointLocal() {
        return referencePointLocal;
    }

    public Coordinate getReferencePointGlobal() {
        return referencePointGlobal;
    }
}
