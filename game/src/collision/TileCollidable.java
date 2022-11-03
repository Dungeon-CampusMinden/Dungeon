package collision;

import level.elements.Tile;
import tools.Point;

public class TileCollidable implements Collidable {
    private Tile tile;
    private Hitbox hitbox;

    public TileCollidable(Tile tile, Hitbox hitbox) {
        this.tile = tile;
        this.hitbox = hitbox;
    }

    @Override
    public Hitbox getHitbox() {
        return hitbox;
    }

    @Override
    public Point getPosition() {
        return tile.getCoordinate().toPoint();
    }

    @Override
    public void colide(Collidable other, CharacterDirection from) {
        // do nothing
    }
}
