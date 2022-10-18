package collision;

import java.util.ArrayList;
import level.elements.ILevel;
import level.elements.Tile;
import tools.Point;

public class CollidableLevel {
    private Collidable[] collidables;

    public void regenHitboxen(ILevel level) {
        ArrayList<Collidable> tiles = new ArrayList<>();
        Tile[][] layout = level.getLayout();
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
