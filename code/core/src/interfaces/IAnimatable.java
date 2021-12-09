package interfaces;

import com.badlogic.gdx.graphics.Texture;
import graphic.Animation;

/** Should be implemented by all objects that have an <code>Animation</code>. */
public interface IAnimatable extends IEntity {

    /** @return the current active <code>Animation</code> (example: idle or run) */
    Animation getActiveAnimation();

    @Override
    default Texture getTexture() {
        return getActiveAnimation().getNextAnimationTexture();
    }
}
