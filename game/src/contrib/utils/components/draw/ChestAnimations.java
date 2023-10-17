package contrib.utils.components.draw;

import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.draw.IPath;

public enum ChestAnimations implements IPath {
    CLOSED("idle_closed", CoreAnimationPriorities.IDLE.priority()),
    OPEN_EMPTY("open_empty", CoreAnimationPriorities.IDLE.priority()),
    OPEN_FULL("open_full", CoreAnimationPriorities.IDLE.priority()),
    opening("open_full", CoreAnimationPriorities.IDLE.priority());


    private final String value;
    private final int priority;

    ChestAnimations(String value, int prio) {
        this.value = value;
        this.priority = prio;
    }

    @Override
    public String pathString() {
        return value;
    }

    @Override
    public String toString() {
        return "ChestAnimation[" + this.value + "]";
    }

    @Override
    public int priority() {
        return priority;
    }
}
