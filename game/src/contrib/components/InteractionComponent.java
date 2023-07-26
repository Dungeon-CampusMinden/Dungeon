package contrib.components;

import com.badlogic.gdx.utils.Null;

import core.Component;
import core.Entity;

import java.util.function.BiConsumer;

/**
 * Allows interaction with the associated entity.
 *
 * <p>An interaction can be triggered using {@link #triggerInteraction(Entity)}. This happens in the
 * {@link core.systems.PlayerSystem} if the player presses the corresponding button on the keyboard
 * and is in the interaction range of this component.
 *
 * <p>What happens during an interaction is defined by the {@link BiConsumer<Entity, Entity>} {@link
 * #onInteraction}.
 *
 * <p>An interaction can be repeatable, in which case it can be triggered multiple times. If an
 * interaction is not repeatable, the {@link InteractionComponent} is removed from the associated
 * entity after the interaction.
 *
 * <p>The interaction radius can be queried with {@link #radius()}.
 */
public final class InteractionComponent extends Component {
    public static final int DEFAULT_INTERACTION_RADIUS = 5;
    public static final boolean DEFAULT_REPEATABLE = true;

    private static final BiConsumer<Entity, Entity> DEFAULT_INTERACTION = (entity, who) -> {};
    private final float radius;
    private final boolean repeatable;
    private final BiConsumer<Entity, Entity> onInteraction;

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
            final BiConsumer<Entity, Entity> onInteraction) {
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
     *
     * @param who The entity that triggered the interaction.
     */
    public void triggerInteraction(@Null Entity who) {
        onInteraction.accept(entity, who);
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
