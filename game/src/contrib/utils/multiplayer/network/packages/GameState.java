package contrib.utils.multiplayer.network.packages;

import core.Entity;
import core.level.elements.ILevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** Used to hold current global/multiplayer game state. */
public class GameState {
    /* Used to collect current level. */
    private ILevel level;
    /* Used to collect current entities except heroes. */
    private Set<Entity> entities = new HashSet<>();
    /* Separate HashMap to collect current 'hero' entities for validation processing. */
    private HashMap<Integer, Entity> heroesByClientId = new HashMap<>();

    /** Creates a new GameState with no level and no heroes. */
    public GameState() {}

    /**
     * Creates a new Instance.
     *
     * @param level Current level.
     * @param entities Current entities.
     * @param heroesByClientId Current heroes according Client IDs.
     */
    public GameState(
            final ILevel level,
            final Set<Entity> entities,
            final HashMap<Integer, Entity> heroesByClientId) {
        this.level = level;
        this.entities = entities;
        this.heroesByClientId = heroesByClientId;
    }

    /**
     * Set the current heroes of the game state.
     *
     * @param heroesByClientId To be set heroes.
     */
    public void heroesByClientId(final HashMap<Integer, Entity> heroesByClientId) {
        this.heroesByClientId = heroesByClientId;
    }

    /**
     * @return the currently existing heroes.
     */
    public HashMap<Integer, Entity> heroesByClientId() {
        return heroesByClientId;
    }

    /**
     * Set the current level of the game state.
     *
     * @param level To be set level.
     */
    public void level(final ILevel level) {
        this.level = level;
    }

    /**
     * @return the currently set level.
     */
    public ILevel level() {
        return level;
    }

    /**
     * Set the current entities of the game state.
     *
     * @param entities To be set entities.
     */
    public void entities(final Set<Entity> entities) {
        this.entities = entities;
    }

    /**
     * @return the currently existing entities.
     */
    public Set<Entity> entities() {
        return entities;
    }

    /** Clear all game state identifiers - level and entities. */
    public void clear() {
        level(null);
        entities(new HashSet<>());
        heroesByClientId(new HashMap<>());
    }
}
