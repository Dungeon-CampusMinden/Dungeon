package ecs.components;

import ecs.entities.Entity;

/*+
 *Component is a piece of data associated with an entity
 */
public abstract class Component {
    protected Entity entity;

    /**
     * @param entity associated with this component
     */

    /**
     * @param entity associated entity
     */
    public Component(Entity entity) {
        this.entity = entity;
    }

    /**
     * @return the associated entity
     */
    public Entity getEntity() {
        return entity;
    }
}
