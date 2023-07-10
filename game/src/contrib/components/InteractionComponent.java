package contrib.components;

import core.Component;
import core.Entity;

import java.util.function.Consumer;

/**
 * Allows interaction with the associated entity.
 *
 * <p>An interaction can be triggered by the player character with a button press when in range.
 *
 * <p>What happens during an interaction is defined by the {@link Consumer<Entity>} {@link
 * #onInteraction}.
 *
 * <p>An interaction can be repeatable, in which case it can be triggered multiple times. If an
 * interaction is not repeatable, the {@link InteractionComponent} is removed from the associated
 * entity after the interaction.
 *
 * <p>The interaction can be triggered using {@link #triggerInteraction()}.
 *
 * <p>The interaction radius can be queried with {@link #radius()}.
 */
public final class InteractionComponent extends Component {
    public static final int DEFAULT_INTERACTION_RADIUS = 5;
    public static final boolean DEFAULT_REPEATABLE = true;

    private static final Consumer<Entity> DEFAULT_INTERACTION = entity -> {};
    private final float radius;
    private final boolean repeatable;
    private final Consumer<Entity> onInteraction;

    /**
     * Create a new {@link InteractionComponent} and adds it to the associated entity.
     *
     * @param entity The associated entity.
     * @param radius The radius in which an interaction can happen.
     * @param repeatable True if the interaction is repeatable, otherwise false.
     * @param onInteraction The behavior that should happen on an interaction.
     */
    public InteractionComponent(
            final Entity entity,
            float radius,
            boolean repeatable,
            final Consumer<Entity> onInteraction) {
        super(entity);
        this.radius = radius;
        this.repeatable = repeatable;
        this.onInteraction = onInteraction;
    }

    /**
     * Create a new {@link InteractionComponent} with default configuration and adds it to the
     * associated entity.
     *
     * <p>The interaction radius is {@link #DEFAULT_INTERACTION_RADIUS}.
     *
     * <p>The interaction callback is empty.
     *
     * @param entity The entity to link to.
     */
    public InteractionComponent(final Entity entity) {
        this(entity, DEFAULT_INTERACTION_RADIUS, DEFAULT_REPEATABLE, DEFAULT_INTERACTION);
    }

    /**
     * Triggers the interaction callback.
     *
     * <p>If the interaction is not repeatable, this component will be removed from the entity
     * afterwards.
     */
    public void triggerInteraction() {
        onInteraction.accept(entity);
        if (!repeatable) entity.removeComponent(InteractionComponent.class);
    }

    /**
     * Gets the interaction radius.
     *
     * @return The radius in which an interaction can happen.
     */
    public float radius() {
        return radius;
    }
}
