package ecs.components;

import ecs.entities.Entity;

public class InteractionComponent extends Component {
    private float radius;
    private boolean repeatable;
    private IInteraction onInteraction;

    public InteractionComponent(
            Entity entity, float radius, boolean repeatable, IInteraction onInteraction) {
        super(entity);
        this.radius = radius;
        this.repeatable = repeatable;
        this.onInteraction = onInteraction;
    }

    public InteractionComponent(Entity entity) {
        this(entity, 5, true, InteractionComponent::DefaultInteraction);
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

    public float getRadius() {
        return radius;
    }
}
