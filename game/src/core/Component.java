package core;

import java.util.logging.Logger;

/**
 * Components store the data (or attributes) for an associated {@link Entity}.
 *
 * <p>This class is the abstract base class for each component.</p>
 *
 * <p>Each component is linked to exactly one entity. Use {@link #getEntity} to get the associated entity
 * of the component.</p>
 *
 * <p>Each component will automatically add itself to the associated entity using {@link Entity#addComponent}.</p>
 *
 * <p>Components are used to describe an entity. {@link System}s will check the components of an entity
 * and decide if they want to process the entity. The systems will then modify the values of the data
 * stored in the components.</p>
 *
 * <p>Remember that an entity can only store one component of each component class.</p>
 */
public abstract class Component {
    protected Entity entity;

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public Component(Entity entity) {
        this.entity = entity;
        entity.addComponent(this);
        Logger componentLogger = Logger.getLogger(this.getClass().getName());
        componentLogger.info(
                "The component '"
                        + this.getClass().getName()
                        + "' was added to entity '"
                        + entity
                        + "'.");
    }

    /**
     * @return the associated entity
     */
    public Entity getEntity() {
        return entity;
    }
}
