package contrib.utils.multiplayer.packages.request;

import contrib.utils.multiplayer.packages.response.LoadMapResponse;
import core.Entity;
import core.level.elements.ILevel;

import java.util.Set;

/**
 * Used to request server to set up game state with given data.
 * <p>According response would be {@link LoadMapResponse}
 */
public class LoadMapRequest {
    private final ILevel level;
    private final Set<Entity> entities;
    private final Entity hero;

    /**
     * Create a new instance.
     *
     * @param currentLevel Level that should be used.
     * @param entities Entities that should be part of the level.
     * @param hero Playable hero.
     */
    public LoadMapRequest(
        final ILevel currentLevel,
        final Set<Entity> entities,
        final Entity hero){
        this.level = currentLevel;
        this.entities = entities;
        this.hero = hero;
    }

    /**
     * @return Level that should be used.
     */
    public ILevel level() {
        return level;
    }

    /**
     * @return Entities that should be part of the level.
     */
    public Set<Entity> entities(){ return entities; }

    /**
     * @return Playable hero.
     */
    public Entity hero() { return hero; }
}
