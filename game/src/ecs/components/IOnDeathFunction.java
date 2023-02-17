package ecs.components;

import ecs.entities.Entity;

/** Functional interfaces for implementing a function that is called when an entity dies */
public interface IOnDeathFunction {

    /**
     * Function that is performed when an entity dies
     *
     * @param entity Entity that has died
     */
    void onDeath(Entity entity);
}
