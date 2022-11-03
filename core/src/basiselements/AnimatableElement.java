package basiselements;

import graphic.Animation;

/**
 * An object that has an <code>Animation</code>.
 *
 * <p>Must be implemented for all objects that should be controlled by the <code>EntityController
 * </code>.
 */
public abstract class AnimatableElement extends DungeonElement {
    /**
     * @return the current active <code>Animation</code> (example: idle or run)
     */
    public abstract Animation getActiveAnimation();

    @Override
    public String getTexturePath() {
        return getActiveAnimation().getNextAnimationTexturePath();
    }
}
