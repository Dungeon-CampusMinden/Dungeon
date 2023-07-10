package contrib.components;

import core.Component;
import core.Entity;

/**
 * Marks an entity to be synchronized by the multiplayer session state.
 *
 * <p>It is used in system {@link contrib.systems.MultiplayerSynchronizationSystem}.
 */
public class MultiplayerSynchronizationComponent extends Component {
    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public MultiplayerSynchronizationComponent(final Entity entity) {
        super(entity);
    }
}
