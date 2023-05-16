package contrib.utils.components.interaction;

import core.Entity;

/**
 * Interface for definition of interaction behavior
 *
 * <p>This Interface is used to define the interaction behavior of an interactive entity. An
 * implementation of this interface can be passed to the {@link
 * contrib.components.InteractionComponent} to define the interaction behavior of the entity
 * associated with that component.
 */
public interface IInteraction {

    /**
     * Implements the interaction behavior of an interactive entity
     *
     * <p>This method is called when an entity interacts with the entity associated with the {@link
     * contrib.components.InteractionComponent} which holds an implementation of this interface.
     *
     * @param entity the entity with which was interacted.
     */
    void onInteraction(Entity entity);
}
