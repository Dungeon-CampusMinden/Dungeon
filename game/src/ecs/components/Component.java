package ecs.components;

import ecs.entities.Entity;

/*+
 *Component is a piece of data associated with an entity
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
    }

    /**
     * @return the associated entity
     */
    public Entity getEntity() {
        return entity;
    }
}
