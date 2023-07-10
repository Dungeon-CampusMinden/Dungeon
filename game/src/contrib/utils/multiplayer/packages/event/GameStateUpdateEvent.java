package contrib.utils.multiplayer.packages.event;

import core.Entity;

import java.util.Set;

/**
 * Used to inform clients about game state changes. Includes only data that will be changed high
 * frequently, like velocity and position.
 */
public class GameStateUpdateEvent {

    private final Set<Entity> entities;

    /**
     * Create a new instance.
     *
     * @param entities Current state of entities.
     */
    public GameStateUpdateEvent(final Set<Entity> entities) {
        this.entities = entities;
    }

    /**
     * @return Entities.
     */
    public Set<Entity> entities() {
        return entities;
    }
}
