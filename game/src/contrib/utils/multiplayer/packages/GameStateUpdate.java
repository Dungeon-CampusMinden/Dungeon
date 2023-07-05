package contrib.utils.multiplayer.packages;

import core.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GameStateUpdate {
    private Set<Entity> entities = new HashSet<>();
    private HashMap<Integer, Entity> heroesByClientId = new HashMap<>();

    public GameStateUpdate(){
    }

    public GameStateUpdate(
        final Set<Entity> entities,
        final HashMap<Integer, Entity> heroesByClientId){
        this.entities = entities;
        this.heroesByClientId = heroesByClientId;
    }

    public HashMap<Integer, Entity> heroesByClientId(){
        return heroesByClientId;
    }

    public Set<Entity> entities() {
        return entities;
    }
}
