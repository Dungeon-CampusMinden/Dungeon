package contrib.systems;


import contrib.components.MultiplayerComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.System;
import contrib.utils.multiplayer.MultiplayerManager;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

// Todo - Adapt to new System change
/** Used to synchronize multiplayer session state with local client state. */
public final class MultiplayerSynchronizationSystem extends System {

    private final MultiplayerManager multiplayerManager;

    /**
     * @param multiplayerManager
     */
    public MultiplayerSynchronizationSystem(final MultiplayerManager multiplayerManager) {
        super(MultiplayerComponent.class);
        this.multiplayerManager = requireNonNull(multiplayerManager);
    }

    @Override
    public void execute() {
        if (multiplayerManager.isConnectedToSession()) {
            if (multiplayerManager.entities() != null && multiplayerManager.entities().stream().count() > 0) {
                synchronizeAddedEntities();
                synchronizeRemovedEntities();
                synchronizePositions();
            }
        } else {
//            removeMultiplayerEntities();
        }
    }

    /* Adds multiplayer entities to local state, that newly joined session. */
    private void synchronizeAddedEntities() {
        final Set<Integer> currentLocalMultiplayerEntityIds =
            Game.entityStream()
//                .filter(entity -> entity.fetch(MultiplayerComponent.class).isPresent())
                .map(entity -> entity.globalID())
                .collect(Collectors.toSet());

        for (Entity multiplayerEntity : multiplayerManager.entities()) {
            boolean isEntityNew = !currentLocalMultiplayerEntityIds.contains(multiplayerEntity.globalID());
            if (isEntityNew) {
                Entity newEntity = new Entity(multiplayerEntity.name());
                newEntity.globalID(multiplayerEntity.globalID());
                multiplayerEntity.components().forEach((key, value) -> {
                    value.entity(newEntity);
                });
            }
        }
    }

    /* Removes multiplayer entities from local state, that are no longer part of multiplayer session. */
    private void synchronizeRemovedEntities() {
        Game.entityStream()
//            .filter(entity -> entity.fetch(MultiplayerComponent.class).isPresent())
            .forEach(entity -> {
                boolean isEntityRemoved =
                    !multiplayerManager.entities().stream().anyMatch(x -> x.globalID() == entity.globalID());
                if (isEntityRemoved) {
                    Game.removeEntity(entity);
                }
            });
    }

    // TODO: not synchronizing position - update velocity component instead for movement animation over velocity system
    /* Synchronizes local positions with positions from multiplayer session. */
    private void synchronizePositions() {
        Game.entityStream()
            .filter(entity -> entity.fetch(PositionComponent.class).isPresent())
            .forEach(localeEntityState -> {
                PositionComponent positionComponentLocale =
                    (PositionComponent) localeEntityState
                        .fetch(PositionComponent.class)
                        .orElseThrow();

                multiplayerManager.entities().stream()
                    .forEach(multiplayerEntityState -> {
                        if (multiplayerEntityState.globalID() == localeEntityState.globalID()) {
                            PositionComponent positionComponentMultiplayer =
                                (PositionComponent) multiplayerEntityState
                                    .fetch(PositionComponent.class)
                                    .orElseThrow();
                            positionComponentLocale.position(positionComponentMultiplayer.position());
                        }
                    });
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
