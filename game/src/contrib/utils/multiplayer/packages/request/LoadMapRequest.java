package contrib.utils.multiplayer.packages.request;

import core.Entity;
import core.level.elements.ILevel;

import java.util.Set;
import java.util.stream.Stream;

public class LoadMapRequest {
    private final ILevel level;
    private final Set<Entity> entities;

    public LoadMapRequest(final ILevel currentLevel, final Set<Entity> entities){
        this.level = currentLevel;
        this.entities = entities;
    }

    public ILevel level() {
        return this.level;
    }

    public Set<Entity> entities(){ return entities; }
}
