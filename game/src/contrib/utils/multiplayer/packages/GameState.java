package contrib.utils.multiplayer.packages;


import core.Entity;
import core.level.elements.ILevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState {
    private static ILevel level;
    private Set<Entity> entities = new HashSet<>();
    private HashMap<Integer, Entity> heroesByClientId = new HashMap<>();

    public GameState(){
    }

    public GameState(
        final ILevel level,
        final Set<Entity> entities,
        final HashMap<Integer, Entity> heroesByClientId){
        this.level = level;
        this.entities = entities;
        this.heroesByClientId = heroesByClientId;
    }

    public void heroesByClientId(final HashMap<Integer, Entity> heroesByClientId){
        this.heroesByClientId = heroesByClientId;
    }

    public HashMap<Integer, Entity> heroesByClientId(){
        return heroesByClientId;
    }

    public void level(final ILevel level) {
        this.level = level;
    }

    public ILevel level() {
        return level;
    }

    public void entities(final Set<Entity> entities) {
        this.entities = entities;
    }

    public Set<Entity> entities() {
        return entities;
    }
}
