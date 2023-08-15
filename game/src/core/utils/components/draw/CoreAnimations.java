package core.utils.components.draw;

/**
 * This enum stores the default paths to the animations, and their priority, used by the systems
 * inside the core package.
 */
public enum CoreAnimations implements IPath {
    IDLE("idle", 1000),
    IDLE_LEFT("idle_left", 1000),
    IDLE_RIGHT("idle_right", 1000),
    IDLE_UP("idle_up", 1000),
    IDLE_DOWN("idle_down", 100),
    RUN("RUN", 2000),
    RUN_LEFT("run_left", 2000),
    RUN_RIGHT("run_right", 2000),
    RUN_UP("run_up", 2000),
    RUN_DOWN("run_down", 2000);

    private final String value;
    private final int priority;

    CoreAnimations(String value, int priority) {
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
