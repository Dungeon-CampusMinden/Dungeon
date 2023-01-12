package ecs.components;

import ecs.entities.Entity;

/** Component is a piece of data associated with an entity */
public abstract class Component {

    private Entity entity;

    public Component(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
