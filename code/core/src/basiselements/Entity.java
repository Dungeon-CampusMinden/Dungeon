package basiselements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import tools.Point;

public abstract class Entity {
    private Painter painter;
    private SpriteBatch batch;

    /**
     * A object that can be controlled by the <code>EntityController
     * </code>.
     */
    public Entity(Painter painter, SpriteBatch batch) {
        this.batch = batch;
        this.painter = painter;
    }

    /** Will be executed every frame. */
    public void update() {}

    /** @return <code>true</code>, if this instance can be deleted; <code>false</code> otherwise */
    public boolean removable() {
        return false;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    /** @return the exact position in the dungeon of this instance */
    public abstract Point getPosition();

    /** @return the (current) Texture-Path of the object */
    public abstract String getTexture();

    /** Each drawable should use this <code>Painter</code> to draw itself. */
    public Painter getPainter() {
        return painter;
    }

    /** Draws this instance on the batch. */
    public void draw() {
        getPainter().draw(getTexture(), getPosition(), getBatch());
    }
}
