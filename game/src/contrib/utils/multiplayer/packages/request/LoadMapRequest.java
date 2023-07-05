package contrib.utils.multiplayer.packages.request;

import core.Entity;
import core.level.elements.ILevel;

import java.util.Set;
import java.util.stream.Stream;

public class LoadMapRequest {
    private final ILevel level;
    private final Set<Entity> entities;
    private final Entity hero;

    public LoadMapRequest(
        final ILevel currentLevel,
        final Set<Entity> entities,
        final Entity hero){
        this.level = currentLevel;
        this.entities = entities;
        this.hero = hero;
    }

    public ILevel level() {
        return level;
    }

    public Set<Entity> entities(){ return entities; }

    public Entity hero() { return hero; }
}
