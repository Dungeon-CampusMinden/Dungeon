package contrib.utils.multiplayer.packages;

import core.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GameStateUpdate {
    private Set<Entity> entities = new HashSet<>();

    public GameStateUpdate(){
    }

    public GameStateUpdate(final Set<Entity> entities){
        this.entities = entities;
    }

    public Set<Entity> entities() {
        return entities;
    }
}
