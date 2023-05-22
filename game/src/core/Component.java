package core;

import java.util.logging.Logger;

/*+
 *Component is a piece of data associated with an entity
 */
public abstract class Component {
    protected Entity entity;

    /**
     * @param entity associated with this component
     */

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
