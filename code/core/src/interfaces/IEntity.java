package interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import tools.Point;

/**
 * Must be implemented for all objects that should be controlled by the <code>EntityController
 * </code>.
 */
public interface IEntity {

    /** Will be executed every frame. */
    void update();

    /** @return <code>true</code>, if this instance can be deleted; <code>false</code> otherwise */
    boolean removable();

    SpriteBatch getBatch();

    /** @return the exact position in the dungeon of this instance */
    Point getPosition();

    /** @return the (current) Texture-Path of the object */
    String getTexture();

    /** Each drawable should use this <code>Painter</code> to draw itself. */
    Painter getPainter();

    /** Draws this instance on the batch. */
    default void draw() {
        getPainter().draw(getTexture(), getPosition(), getBatch());
    }
}
