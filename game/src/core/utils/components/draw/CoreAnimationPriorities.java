package core.utils.components.draw;

public enum CoreAnimationPriorities {
    IDLE(1000),
    RUN(2000);

    private final int priority;

    CoreAnimationPriorities(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
