package contrib.systems;


import contrib.entities.EntityFactory;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.System;
import contrib.components.MultiplayerComponent;
import mp.MultiplayerAPI;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

// Todo - Adapt to new System change
/** Used to synchronize multiplayer session state with local client state. */
public final class MultiplayerSynchronizationSystem extends System {

    private final MultiplayerAPI multiplayerAPI;

    /**
     * @param multiplayerAPI
     */
    public MultiplayerSynchronizationSystem(final MultiplayerAPI multiplayerAPI) {
        // Todo - Look if Multiplayercomponent the only necessary component is
        super(MultiplayerComponent.class);
        this.multiplayerAPI = requireNonNull(multiplayerAPI);
    }

    @Override
    public void execute() {
        if (multiplayerAPI.isConnectedToSession()) {
            if (multiplayerAPI.getHeroPositionByPlayerId() != null) {
                synchronizeAddedEntities();
                synchronizeRemovedEntities();
                synchronizePositions();
            }
        } else {
            removeMultiplayerEntities();
        }
    }

    /* Adds multiplayer entities to local state, that newly joined session. */
    private void synchronizeAddedEntities() {
        final Set<Integer> currentLocalMultiplayerEntityIds =
            Game.entityStream()
                .flatMap(e -> e.fetch(MultiplayerComponent.class).stream())
                .map(component -> ((MultiplayerComponent)component).getPlayerId())
                .collect(Collectors.toSet());
        multiplayerAPI.getHeroPositionByPlayerId()
            .forEach((Integer playerId, Point position) -> {
                boolean isOwnHero = playerId == multiplayerAPI.getOwnPlayerId();
                if (!isOwnHero) {
                    boolean isHeroNewJoined = !currentLocalMultiplayerEntityIds.contains(playerId);
                    if(isHeroNewJoined) {
                        try {
                            EntityFactory.newHeroDummy(playerId);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        });
    }

    /* Removes multiplayer entities from local state, that are no longer part of multiplayer session. */
    private void synchronizeRemovedEntities() {
        Game.entityStream()
            .flatMap(entity -> entity.fetch(MultiplayerComponent.class).stream())
            .map(multiplayerComponent -> (MultiplayerComponent) multiplayerComponent)
            .forEach(multiplayerComponent -> {
                boolean isOwnHero = multiplayerComponent.getPlayerId() == multiplayerAPI.getOwnPlayerId();
                boolean isEntityRemoved =
                    !multiplayerAPI.getHeroPositionByPlayerId().containsKey(multiplayerComponent.getPlayerId());
                if (!isOwnHero && isEntityRemoved) Game.removeEntity(multiplayerComponent.entity());
            });
    }

    // TODO: not synchronizing position - update velocity component instead for movement animation over velocity system
    /* Synchronizes local positions with positions from multiplayer session. */
    private void synchronizePositions() {
        Game.entityStream()
            .flatMap(entity -> entity.fetch(MultiplayerComponent.class).stream())
            .map(multiplayerComponent -> (MultiplayerComponent) multiplayerComponent)
            .forEach(multiplayerComponent -> {
                    PositionComponent positionComponent =
                        (PositionComponent) multiplayerComponent
                            .entity()
                            .fetch(PositionComponent.class)
                            .orElseThrow();
                    Point currentPositionAtMultiplayerSession =
                        multiplayerAPI.getHeroPositionByPlayerId().get(multiplayerComponent.getPlayerId());
                    positionComponent.position(currentPositionAtMultiplayerSession);
            });
    }

    /* Removes all entities that has been added due to multiplayer session from local state */
    private void removeMultiplayerEntities() {
        Game.entityStream()
            .forEach(entity -> {
                if (entity.fetch(MultiplayerComponent.class).isPresent()) {
                    Game.removeEntity(entity);
                }
            });
    }
}
