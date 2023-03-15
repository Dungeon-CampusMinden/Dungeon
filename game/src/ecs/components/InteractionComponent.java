package ecs.components;

import ecs.entities.Entity;

public class InteractionComponent extends Component {
    public static final int DEFAULT_RADIUS = 5;
    public static final boolean DEFAULT_REPEATABLE = true;
    private float radius;
    private boolean repeatable;
    private IInteraction onInteraction;

    /**
     * complex ctor which allows the attribuhtes to be configured
     *
     * @param entity the entity to link to
     * @param radius the radius in which an interaction can happen
     * @param repeatable true if the interaction is repeatable, otherwise false
     * @param onInteraction the strategy which should happen on an interaction
     */
    public InteractionComponent(
            Entity entity, float radius, boolean repeatable, IInteraction onInteraction) {
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
    public InteractionComponent(Entity entity) {
        this(entity, DEFAULT_RADIUS, DEFAULT_REPEATABLE, InteractionComponent::DefaultInteraction);
    }

    /** triggers the interaction between hero and the Entity of the component */
    public void triggerInteraction() {
        onInteraction.onInteraction(entity);
        if (!repeatable) entity.removeComponent(InteractionComponent.class);
    }

    /**
     * simple default interaction which helps to get started
     *
     * @param e the Entity which interacts with the current
     */
    public static void DefaultInteraction(Entity e) {
        System.out.println(e.id + " did use the DefaultInteraction");
    }

    /**
     * @return the radius in which an interaction can happen
     */
    public float getRadius() {
        return radius;
    }
}
