package basiselements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.HUDPainter;
import tools.Point;

public abstract class HUDElement {
    private SpriteBatch batch;
    private HUDPainter painter;
    /**
     * A object that can be controlled by the <code>HUDController
     * </code>.
     */
    public HUDElement(HUDPainter painter, SpriteBatch batch) {
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
    public HUDPainter getPainter() {
        return painter;
    }

    /** Draws this instance on the batch. */
    public void draw() {
        getPainter().draw(getTexture(), getPosition(), getBatch());
    }

    /**
     * Draws this instance on the batch with a scaling.
     *
     * @param xScaling x-scale
     * @param yScaling y-scale
     */
    public void drawWithScaling(float xScaling, float yScaling) {
        getPainter().drawWithScaling(xScaling, yScaling, getTexture(), getPosition(), getBatch());
    }
}
