package core.utils.components.draw;

/**
 * This enum stores the default paths to the animations used by the systems inside the core package.
 */
public enum CoreAnimationPathEnum implements IAnimationPathEnum {
    IDLE_LEFT("idleLeft"),
    IDLE_RIGHT("idleRight"),
    RUN_LEFT("runLeft"),
    RUN_RIGHT("runRight");

    private final String value;

    CoreAnimationPathEnum(String value) {
        this.value = value;
    }
}
