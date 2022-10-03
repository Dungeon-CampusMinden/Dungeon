package collision;

import tools.Point;

public interface Colideable {
    Hitbox getHitbox();

    Point getPosition();

    void colide(Colideable other, Direction from);

    enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NONE
    }
}
