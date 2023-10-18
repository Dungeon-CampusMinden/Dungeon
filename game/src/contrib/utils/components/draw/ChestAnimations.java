package contrib.utils.components.draw;

import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.draw.IPath;

public enum ChestAnimations implements IPath {
    CLOSED("idle_closed", CoreAnimationPriorities.IDLE.priority()),
    // once the chest is open there are two states with items or without
    OPEN_EMPTY("open_empty", CoreAnimationPriorities.IDLE.priority() + 100),
    OPEN_FULL("open_full", CoreAnimationPriorities.IDLE.priority() + 100),
    // animation
    OPENING("opening", CoreAnimationPriorities.IDLE.priority() + 200);

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
