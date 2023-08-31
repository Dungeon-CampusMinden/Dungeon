package contrib.utils.components.draw;

public enum AdditionalAnimationsPriorities {
    DIE(5000),
    HIT(4000),
    FIGHT(3000);

    private final int priority;

    AdditionalAnimationsPriorities(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
