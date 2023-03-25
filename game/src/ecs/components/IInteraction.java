package ecs.components;

import ecs.entities.Entity;
import java.io.Serializable;

public interface IInteraction extends Serializable {

    /**
     * Implements the interaction behavior of an Interactive entity
     *
     * @param entity
     */
    void onInteraction(Entity entity);
}
