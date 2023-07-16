package contrib.utils.multiplayer.network.packages;

import core.Entity;

import java.util.Set;

/** Used to inform clients about new game state tick wise. */
public class GameStateUpdate {
    private final Set<Entity> entities;

    /**
     * Creates a new Instance.
     *
     * @param entities Current entities of the game state.
     */
    public GameStateUpdate(final Set<Entity> entities) {
        this.entities = entities;
    }

    /**
     * @return Entities.
     */
    public Set<Entity> entities() {
        return entities;
    }
}
