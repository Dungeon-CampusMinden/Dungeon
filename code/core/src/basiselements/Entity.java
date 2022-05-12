package basiselements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;

public abstract class Entity extends DungeonElement {
    private Painter painter;

    /**
     * A object that can be controlled by the <code>EntityController
     * </code>.
     *
     * @param painter Painter that draws this object
     * @param batch Batch to draw on
     */
    public Entity(Painter painter, SpriteBatch batch) {
        super(batch);
        this.painter = painter;
    }

    /** Each drawable should use this <code>Painter</code> to draw itself. */
    public Painter getPainter() {
        return painter;
    }

    /** Draws this instance on the batch. */
    @Override
    public void draw() {
        getPainter().draw(getTexturePath(), getPosition(), getBatch());
    }
}
