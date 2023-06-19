package contrib.utils.components.draw;

import core.utils.components.draw.IPath;

/**
 * This enum stores the paths to the animations used by the systems inside the contrib package.
 *
 * <p>Add your own path, if you need a new animation-type (like jumping)
 *
 * @see core.components.DrawComponent
 * @see IPath
 * @see core.utils.components.draw.CoreAnimations
 */
public enum AdditionalAnimations implements IPath {
    DIE("die"),
    HIT("hit"),
    ATTACK("attack");

    private final String value;

    AdditionalAnimations(String value) {
        this.value = value;
    }

    @Override
    public String pathString() {
        return value;
    }
}
