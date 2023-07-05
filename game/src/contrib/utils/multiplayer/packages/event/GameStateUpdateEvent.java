package contrib.utils.multiplayer.packages.event;

import contrib.utils.multiplayer.packages.GameState;
import core.Entity;
import core.level.elements.ILevel;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameStateUpdateEvent {

    private final Set<Entity> entities;
    private final HashMap<Integer, Entity> heroesByClientId;

    public GameStateUpdateEvent(
        final HashMap<Integer, Entity> heroesByClientId,
        final Set<Entity> entities){
        this.entities = entities;
        this.heroesByClientId = heroesByClientId;
    }

    public HashMap<Integer, Entity> heroesByClientId(){
        return heroesByClientId;
    }

    public Set<Entity> entities() { return entities; }
}
