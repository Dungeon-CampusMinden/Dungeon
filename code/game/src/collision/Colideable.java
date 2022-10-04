package collision;

import tools.Point;

/** Implemented by Objects that can colide with each other */
public interface Colideable {
    /**
     * @return The Hitbox of the Object
     */
    Hitbox getHitbox();

    /**
     * @return The Position of the Object
     */
    Point getPosition();

    /**
     * Action to do on a collision
     *
     * @param other Object you colide with
     * @param from Direction from where you colide
     */
    void colide(Colideable other, CharacterDirection from);
}
