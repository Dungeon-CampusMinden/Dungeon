package contrib.components;

import core.Component;
import core.Entity;

import java.util.function.Consumer;

public final class InteractionComponent extends Component {
    public static final int DEFAULT_RADIUS = 5;
    public static final boolean DEFAULT_REPEATABLE = true;

    private static final Consumer<Entity> DEFAULT_INTERACTION = entity -> {};
    private final float radius;
    private final boolean repeatable;
    private final Consumer<Entity> onInteraction;

    /**
     * complex ctor which allows the attributes to be configured
     *
     * @param entity the entity to link to
     * @param radius the radius in which an interaction can happen
     * @param repeatable true if the interaction is repeatable, otherwise false
     * @param onInteraction the strategy which should happen on an interaction
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
     * simple ctor which sets all attributes to the default values
     *
     * @param entity the entity to link to
     */
    public InteractionComponent(final Entity entity) {
        this(entity, DEFAULT_RADIUS, DEFAULT_REPEATABLE, DEFAULT_INTERACTION);
    }

    /** triggers the interaction between hero and the Entity of the component */
    public void triggerInteraction() {
        onInteraction.accept(entity);
        if (!repeatable) entity.removeComponent(InteractionComponent.class);
    }

    /**
     * @return the radius in which an interaction can happen
     */
    public float radius() {
        return radius;
    }
}
