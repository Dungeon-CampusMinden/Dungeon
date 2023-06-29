package mp.packages.request;

import core.Entity;
import core.level.elements.ILevel;
import core.utils.Point;

import java.util.Set;

public class LoadMapRequest {
    private final ILevel level;
    private final Set<Entity> currentEntities;

    public LoadMapRequest(final ILevel currentLevel, final Set<Entity> currentEntities){
        this.level = currentLevel;
        this.currentEntities = currentEntities;
    }

    public ILevel getLevel() {
        return this.level;
    }

    public Set<Entity> getCurrentEntities(){ return currentEntities; }
}
