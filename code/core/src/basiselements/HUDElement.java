package basiselements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.HUDPainter;

public abstract class HUDElement extends DungeonElement {
    private HUDPainter painter;

    /**
     * A object that can be controlled by the <code>HUDController
     * </code>.
     *
     * @param painter Painter that draws this object
     * @param batch Batch to draw on
     */
    public HUDElement(HUDPainter painter, SpriteBatch batch) {
        super(batch);
        this.painter = painter;
    }

    /** Each drawable should use this <code>Painter</code> to draw itself. */
    public HUDPainter getPainter() {
        return painter;
    }

    /** Draws this instance on the batch. */
    @Override
    public void draw() {
        getPainter().draw(getTexturePath(), getPosition(), getBatch());
    }

    /**
     * Draws this instance on the batch with a scaling.
     *
     * @param xScaling x-scale
     * @param yScaling y-scale
     */
    public void drawWithScaling(float xScaling, float yScaling) {
        getPainter()
                .drawWithScaling(xScaling, yScaling, getTexturePath(), getPosition(), getBatch());
    }
}
