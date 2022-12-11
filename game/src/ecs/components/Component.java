package ecs.components;

import ecs.entities.Entity;

C/*+
 *Component is a piece of data associated with an entity
 */
public abstract class Component {
    protected Entity entity;

    /**
     * @param entity associated with this component
     */
    public Component(Entity entity) {
        this.entity = entity;
    }

    /**
     * @return the entity that this component is attached to
     */
    public Entity getEntity() {
        return entity;
    }
}
