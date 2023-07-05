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

    public GameStateUpdateEvent(final Set<Entity> entities){
        this.entities = entities;
    }

    public Set<Entity> entities() { return entities; }
}
