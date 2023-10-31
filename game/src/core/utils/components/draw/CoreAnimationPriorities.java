package core.utils.components.draw;

/**
 * CoreAnimationPriorities contain animations which usually all entities have. which contain simple
 * idle animations, movement animations, and the default animations.
 */
public enum CoreAnimationPriorities {
    IDLE(1000),
    RUN(2000),
    DEFAULT(0);

    private final int priority;

    CoreAnimationPriorities(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
