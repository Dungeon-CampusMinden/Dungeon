package collision;

import java.util.ArrayList;
import level.elements.Tile;
import level.elements.TileLevel;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import tools.Point;

public class CollidableLevel extends TileLevel {
    private Collidable[] collidables;

    public CollidableLevel(Tile[][] layout) {
        super(layout);
    }

    public CollidableLevel(LevelElement[][] layout, DesignLabel designLabel) {
        super(layout, designLabel);
    }

    public void regenHitboxen() {
        ArrayList<Collidable> tiles = new ArrayList<>();
        Tile[][] layout = this.getLayout();
        for (var tilerow : layout) {
            for (var tile : tilerow) {
                if (!tile.isAccessible()) {
                    TileCollidable tileCollidable = new TileCollidable(tile, new Hitbox(16, 16));
                    tileCollidable.getHitbox().setCollidable(tileCollidable);

                    tiles.add(tileCollidable);
                }
            }
        }
        int width = layout[0].length;
        int height = layout.length;
        // the level borders
        tiles.add(new RectCollidable(new Point(-1, -1), 16 * (width + 2), 16)); // bottom
        tiles.add(new RectCollidable(new Point(-1, -1), 16, 16 * (height + 2))); // left
        tiles.add(new RectCollidable(new Point(width, -1), 16, 16 * (height + 2))); // right
        tiles.add(new RectCollidable(new Point(-1, height), 16 * (width + 2), 16)); // top
        collidables = tiles.toArray(new Collidable[0]);
    }

    public Collidable[] getCollidables() {
        return collidables;
    }
}
