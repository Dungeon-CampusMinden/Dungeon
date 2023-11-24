package core.utils.components.draw;

import core.utils.components.path.IPath;

/**
 * Default paths to the animations, and their priority, used by the systems inside the core package.
 *
 * <p>The enums represent a path (as a string) where animations can be found. The path starts at a
 * specific subfolder (e.g., "character/hero"), so the enums have the value of the directory inside
 * this directory (e.g., "character/hero/idle_left").
 *
 * <p>You can use this values for {@link core.components.DrawComponent#queueAnimation(IPath...)}.
 */
public enum CoreAnimations implements IPath {
    IDLE("idle", CoreAnimationPriorities.IDLE.priority()),
    IDLE_LEFT("idle_left", CoreAnimationPriorities.IDLE.priority()),
    IDLE_RIGHT("idle_right", CoreAnimationPriorities.IDLE.priority()),
    IDLE_UP("idle_up", CoreAnimationPriorities.IDLE.priority()),
    IDLE_DOWN("idle_down", CoreAnimationPriorities.IDLE.priority()),
    RUN("run", CoreAnimationPriorities.RUN.priority()),
    RUN_LEFT("run_left", CoreAnimationPriorities.RUN.priority()),
    RUN_RIGHT("run_right", CoreAnimationPriorities.RUN.priority()),
    RUN_UP("run_up", CoreAnimationPriorities.RUN.priority()),
    RUN_DOWN("run_down", CoreAnimationPriorities.RUN.priority());

    private final String value;
    private final int priority;

    CoreAnimations(final String value, int priority) {
        this.value = value;
        this.priority = priority;
    }

    @Override
    public String pathString() {
        return value;
    }

    @Override
    public String toString() {
        return "CoreAnimation[" + this.value + "]";
    }

    @Override
    public int priority() {
        return priority;
    }
}
