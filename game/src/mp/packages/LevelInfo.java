package mp.packages;

import core.Entity;
import core.level.elements.ILevel;

import java.util.Set;

public class LevelInfo {
    private ILevel level;
    private Set<Entity> currentEntities;

    public LevelInfo(){
    }

    public LevelInfo(ILevel level, Set<Entity> currentEntities){
        this.level = level;
        this.currentEntities = currentEntities;
    }

    public void setLevel(ILevel level) {
        this.level = level;
    }

    public ILevel getLevel() {
        return level;
    }

    public Set<Entity> getCurrentEntities() {
        return currentEntities;
    }

    public void setCurrentEntities(Set<Entity> currentEntities) {
        this.currentEntities = currentEntities;
    }
}
