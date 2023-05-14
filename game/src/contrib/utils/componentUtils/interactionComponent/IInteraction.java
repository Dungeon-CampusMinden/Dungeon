package contrib.utils.componentUtils.interactionComponent;

import core.Entity;

public interface IInteraction {

    /**
     * Implements the interaction behavior of an Interactive entity
     *
     * @param entity
     */
    void onInteraction(Entity entity);
}
