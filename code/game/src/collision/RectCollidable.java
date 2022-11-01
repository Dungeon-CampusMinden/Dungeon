package collision;

import tools.Point;

public class RectCollidable implements Collidable {
    private Point position;
    private float width;
    private float height;

    private Hitbox hitbox;

    public RectCollidable(Point position, float width, float height) {
        this.position = position;
        this.width = width;
        this.height = height;
        hitbox = new Hitbox((int) width, (int) height);
        hitbox.setCollidable(this);
    }

    public RectCollidable(Point position, int width, int height) {
        this.position = position;
        this.width = width / 16f;
        this.height = height / 16f;
        hitbox = new Hitbox((int) width, (int) height);
        hitbox.setCollidable(this);
    }

    @Override
    public Hitbox getHitbox() {
        return hitbox;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public void colide(Collidable other, CharacterDirection from) {}
}
