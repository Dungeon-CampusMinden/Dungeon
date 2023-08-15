package contrib.utils.components.draw;

import core.utils.components.draw.IPath;

/**
 * This enum stores the paths to the animations, and their priority, used by the systems inside the
 * contrib package.
 *
 * <p>Add your own path, if you need a new animation-type (like jumping)
 *
 * @see core.components.DrawComponent
 * @see IPath
 * @see core.utils.components.draw.CoreAnimations
 */
public enum AdditionalAnimations implements IPath {
    DIE("die", 5000),
    DIE_LEFT("die_left", 5000),
    DIE_RIGHT("die_right", 5000),
    DIE_UP("die_up", 5000),
    DIE_DOWN("die_down", 5000),
    HIT("hit", 4000),
    ATTACK("attack", 3000),
    FIGHT_LEFT("fight_left", 3000),
    FIGHT_RIGHT("fight_right", 3000),
    FIGHT_UP("fight_up", 3000),
    FIGHT_DOWN("fight_down", 3000);

    private final String value;
    private final int priority;

    AdditionalAnimations(String value, int prio) {
        this.value = value;
        this.priority = prio;
    }

    @Override
    public String pathString() {
        return value;
    }

    @Override
    public String toString() {
        return "AdditionalAnimation[" + this.value + "]";
    }

    @Override
    public int priority() {
        return priority;
    }
}
