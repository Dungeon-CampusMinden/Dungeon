package basiselements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Animation;
import graphic.Painter;

/** A object that has an <code>Animation</code>. */
public abstract class Animatable extends Entity {

    /**
     * Must be implemented for all objects that should be controlled by the <code>EntityController
     * </code>.
     *
     * @param painter Painter that draws this object
     * @param batch Batch to draw on
     */
    public Animatable(Painter painter, SpriteBatch batch) {
        super(painter, batch);
    }

    /** @return the current active <code>Animation</code> (example: idle or run) */
    public abstract Animation getActiveAnimation();

    @Override
    public String getTexturePath() {
        return getActiveAnimation().getNextAnimationTexturePath();
    }
}
