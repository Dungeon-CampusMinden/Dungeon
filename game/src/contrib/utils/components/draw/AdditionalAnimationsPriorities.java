package contrib.utils.components.draw;

/** Collection of animations for entities which can attack or get attacked. */
public enum AdditionalAnimationsPriorities {
    DIE(5000),
    HIT(4000),
    FIGHT(3000);

    private final int priority;

    AdditionalAnimationsPriorities(final int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
