package level.elements;

import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.TileTextureFactory;
import tools.Point;

import java.util.Random;

public class Room {
    private Tile[][] layout;
    private DesignLabel design;
    private Point referencePointGlobal;
    private Point referencePointLocal;

    public Room(
            LevelElement[][] layout,
            DesignLabel label,
            Point referencePointLocal,
            Point referencePointGlobal) {
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

    private void convertLayout(LevelElement[][] toConvert) {
        float difx = referencePointGlobal.x - referencePointLocal.x;
        float dify = referencePointGlobal.y - referencePointLocal.y;

        for (int y = 0; y < toConvert.length; y++)
            for (int x = 0; x < toConvert[0].length; x++) {
                Point p = new Point(x, y);
                String texture =
                        TileTextureFactory.findTexture(toConvert[y][x], design, toConvert, p);
                layout[y][x] = new Tile(texture, new Point(x + difx, y + dify), toConvert[y][x]);
            }
    }

    public Tile[][] getLayout() {
        // copy of the layout
        return copyLayout(layout);
    }

    public void setLayout(Tile[][] layout) {
        this.layout = copyLayout(layout);
    }

    private Tile[][] copyLayout(Tile[][] toCopy) {
        Tile[][] copy = new Tile[layout.length][layout[0].length];
        for (int y = 0; y < toCopy.length; y++)
            for (int x = 0; x < toCopy[0].length; x++) {
                copy[y][x] = toCopy[y][x];
            }
        return copy;
    }

    /** @return random floor-tile in the room */
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

    public DesignLabel getDesign() {
        return design;
    }
}
