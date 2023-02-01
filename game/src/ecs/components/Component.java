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
     * @param name Name of the Component
     */
    public Component(Entity entity, String name) {
        this.entity = entity;
        entity.addComponent(name, this);
    }

    /**
     * @return the associated entity
     */
    public Entity getEntity() {
        return entity;
    }
}
