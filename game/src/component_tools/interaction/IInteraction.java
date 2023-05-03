package component_tools.interaction;

import entities.Entity;

public interface IInteraction {

    /**
     * Implements the interaction behavior of an Interactive entity
     *
     * @param entity
     */
    void onInteraction(Entity entity);
}
