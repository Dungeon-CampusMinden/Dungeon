package contrib.systems;


import contrib.components.MultiplayerComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.System;
import contrib.utils.multiplayer.MultiplayerAPI;

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
        super(MultiplayerComponent.class);
        this.multiplayerAPI = requireNonNull(multiplayerAPI);
    }

    @Override
    public void execute() {
        if (multiplayerAPI.isConnectedToSession()) {
            if (multiplayerAPI.entities() != null && multiplayerAPI.entities().stream().count() > 0) {
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

        for (Entity multiplayerEntity : multiplayerAPI.entities()) {
            boolean isOwnHero = multiplayerEntity.globalID() == Game.hero().get().globalID();
            if (!isOwnHero) {
                boolean isEntityNew = !currentLocalMultiplayerEntityIds.contains(multiplayerEntity.globalID());
                if (isEntityNew) {
                    Entity newEntity = new Entity(multiplayerEntity.name());
                    newEntity.globalID(newEntity.globalID());
                    multiplayerEntity.components().forEach((key, value) -> {
                        value.entity(newEntity);
                    });
                }
            }
        }
    }

    /* Removes multiplayer entities from local state, that are no longer part of multiplayer session. */
    private void synchronizeRemovedEntities() {
        Game.entityStream()
//            .filter(entity -> entity.fetch(MultiplayerComponent.class).isPresent())
            .forEach(entity -> {
                boolean isOwnHero = entity.globalID() == Game.hero().get().globalID();
                boolean isEntityRemoved =
                    !multiplayerAPI.entities().stream().anyMatch(x -> x.globalID() == entity.globalID());
                if (!isOwnHero && isEntityRemoved) {
                    Game.removeEntity(entity);
                }
            });
    }

    // TODO: not synchronizing position - update velocity component instead for movement animation over velocity system
    /* Synchronizes local positions with positions from multiplayer session. */
    private void synchronizePositions() {
        Game.entityStream()
//            .filter(entity -> entity.fetch(MultiplayerComponent.class).isPresent())
            .forEach(localeEntityState -> {
                PositionComponent positionComponentLocale =
                    (PositionComponent) localeEntityState
                        .fetch(PositionComponent.class)
                        .orElseThrow();

                multiplayerAPI.entities().stream()
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
