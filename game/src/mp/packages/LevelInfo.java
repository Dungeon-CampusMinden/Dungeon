package mp.packages;

import core.Entity;
import core.level.elements.ILevel;

import java.util.Set;
import java.util.stream.Stream;

public class LevelInfo {
    private ILevel level;
    private Stream<Entity> currentEntities;

    public LevelInfo(){
    }

    public LevelInfo(ILevel level, Stream<Entity> currentEntities){
        this.level = level;
        this.currentEntities = currentEntities;
    }

    public void setLevel(ILevel level) {
        this.level = level;
    }

    public ILevel getLevel() {
        return level;
    }

    public Stream<Entity> getCurrentEntities() {
        return currentEntities;
    }

    public void setCurrentEntities(Stream<Entity> currentEntities) {
        this.currentEntities = currentEntities;
    }
}
