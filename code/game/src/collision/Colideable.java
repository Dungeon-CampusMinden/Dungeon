package collision;

import tools.Point;

public interface Colideable {
    Hitbox getHitbox();

    Point getPosition();

    void colide(Colideable other, CharacterDirection from);
}
