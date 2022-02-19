package interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.HUDPainter;
import tools.Point;

public interface IHUDElement {
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
    HUDPainter getPainter();

    /** Draws this instance on the batch. */
    default void draw() {
        getPainter().draw(getTexture(), getPosition(), getBatch());
    }
}
