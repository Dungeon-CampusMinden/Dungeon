package core.utils.components.draw;

/**
 * This enum stores the default paths to the animations used by the systems inside the core package.
 */
public enum CoreAnimations implements IPath {
    IDLE_LEFT("idleLeft"),
    IDLE_RIGHT("idleRight"),
    RUN_LEFT("runLeft"),
    RUN_RIGHT("runRight"),
    RUN_FORWARD("run_forward"),
    RUN_BACKWARD("run_backward"),
    FIGHT_BACK("fight_back"),
    FIGHT_FRONT("fight_front"),
    FIGHT_LEFT("fight_left"),
    FIGHT_RIGHT("fight_right"),
    DIE_BACK("die_back"),
    DIE_FRONT("die_front"),
    DIE_LEFT("die_left"),
    DIE_RIGHT("die_right");

    private final String value;

    CoreAnimations(String value) {
        this.value = value;
    }

    @Override
    public String pathString() {
        return value;
    }
}
