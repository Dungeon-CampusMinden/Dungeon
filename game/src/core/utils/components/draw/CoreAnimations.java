package core.utils.components.draw;

/**
 * This enum stores the default paths to the animations used by the systems inside the core package.
 */
public enum CoreAnimations implements IPath {
    IDLE("idle"),
    IDLE_LEFT("idle_left"),
    IDLE_RIGHT("idle_right"),
    IDLE_UP("idle_up"),
    IDLE_DOWN("idle_down"),
    RUN_LEFT("run_left"),
    RUN_RIGHT("run_right"),
    RUN_UP("run_up"),
    RUN_DOWN("run_down");

    private final String value;

    CoreAnimations(String value) {
        this.value = value;
    }

    @Override
    public String pathString() {
        return value;
    }
}
