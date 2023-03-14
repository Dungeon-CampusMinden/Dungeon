package ecs.components;

import ecs.entities.Entity;
import java.io.Serializable;

/** Functional interfaces for implementing a function that is called when an entity dies */
public interface IOnDeathFunction extends Serializable {

    /**
     * Function that is performed when an entity dies
     *
     * @param entity Entity that has died
     */
    void onDeath(Entity entity);
}
