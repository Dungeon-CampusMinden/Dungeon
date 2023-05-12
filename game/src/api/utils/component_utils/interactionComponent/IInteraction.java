package api.utils.component_utils.interactionComponent;

import api.Entity;

public interface IInteraction {

    /**
     * Implements the interaction behavior of an Interactive entity
     *
     * @param entity
     */
    void onInteraction(Entity entity);
}
