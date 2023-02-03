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
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     * @param klaas Class of the Component
     */
    public Component(Entity entity, Class klaas) {
        this.entity = entity;
        entity.addComponent(klaas, this);
    }

    /**
     * @return the associated entity
     */
    public Entity getEntity() {
        return entity;
    }
}
