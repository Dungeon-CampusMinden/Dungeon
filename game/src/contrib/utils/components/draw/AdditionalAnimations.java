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
    DIE_LEFT("die_left"),
    DIE_RIGHT("die_right"),
    DIE_UP("die_up"),
    DIE_DOWN("die_down"),
    HIT("hit"),
    ATTACK("attack"),
    FIGHT_LEFT("fight_left"),
    FIGHT_RIGHT("fight_right"),
    FIGHT_UP("fight_up"),
    FIGHT_DOWN("fight_down");

    private final String value;

    AdditionalAnimations(String value) {
        this.value = value;
    }

    @Override
    public String pathString() {
        return value;
    }
}
