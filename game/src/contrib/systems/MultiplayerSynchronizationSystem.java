package contrib.systems;

import static java.util.Objects.requireNonNull;

import contrib.components.MultiplayerSynchronizationComponent;
import contrib.utils.multiplayer.MultiplayerManager;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This system synchronizes multiplayer session entities state with local state.
 *
 * <p>Each entity with a {@link MultiplayerSynchronizationComponent} will be synchronized.
 *
 * <p>This system will compare entities of global/multiplayer state and local state and adds or/and
 * remove entities to/from the local state in {@link Game}.
 *
 * <p>The system will get the current position and animation from the {@link PositionComponent} and
 * {@link DrawComponent} of the global/multiplayer state and will update the current position and
 * next animation frame from the {@link Animation} for the local state.
 *
 * @see MultiplayerSynchronizationComponent
 */
public final class MultiplayerSynchronizationSystem extends System {

    /** Used to get global/multiplayer state. */
    private final MultiplayerManager multiplayerManager;

    /**
     * Create a new MultiplayerSynchronizationSystem to synchronize global and local state of
     * entities.
     *
     * @param multiplayerManager The multiplayer instance that holds global state.
     * @see MultiplayerManager
     */
    public MultiplayerSynchronizationSystem(final MultiplayerManager multiplayerManager) {
        super(MultiplayerSynchronizationComponent.class);
        this.multiplayerManager = requireNonNull(multiplayerManager);
    }

    @Override
    public void execute() {
        if (multiplayerManager.isConnectedToSession()) {
            if (multiplayerManager.entityStream() != null
                    && multiplayerManager.entityStream().findAny().isPresent()) {
                synchronizeAddedEntities();
                synchronizeRemovedEntities();
                synchronizePositions();
                synchronizeAnimation();
            }
        } else {
            removeMultiplayerEntities();
        }
    }

    /** Adds multiplayer entities to local state, that newly joined session. */
    private void synchronizeAddedEntities() {
        final Set<Integer> currentLocalMultiplayerEntityIds =
                Game.entityStream()
                        //                .filter(entity ->
                        // entity.fetch(MultiplayerComponent.class).isPresent())
                        .map(Entity::globalID)
                        .collect(Collectors.toSet());

        multiplayerManager
                .entityStream()
                .forEach(
                        multiplayerEntity -> {
                            boolean isEntityNew =
                                    !currentLocalMultiplayerEntityIds.contains(
                                            multiplayerEntity.globalID());
                            if (isEntityNew) {
                                Entity newEntity = new Entity(multiplayerEntity.name());
                                /* Global ID has to be set so that each entity can be identified in multiplayer session. */
                                newEntity.globalID(multiplayerEntity.globalID());
                                multiplayerEntity
                                        .components()
                                        .forEach((key, value) -> value.entity(newEntity));
                            }
                        });
    }

    /**
     * Removes multiplayer entities from local state, that are no longer part of multiplayer
     * session.
     */
    private void synchronizeRemovedEntities() {
        Game.entityStream()
                //            .filter(entity ->
                // entity.fetch(MultiplayerComponent.class).isPresent())
                .forEach(
                        entity -> {
                            boolean isEntityRemoved =
                                    multiplayerManager
                                            .entityStream()
                                            .noneMatch(x -> x.globalID() == entity.globalID());
                            if (isEntityRemoved) {
                                Game.removeEntity(entity);
                            }
                        });
    }

    /** Synchronizes local positions with positions from global/multiplayer session. */
    private void synchronizePositions() {
        Game.entityStream()
                .filter(entity -> entity.fetch(PositionComponent.class).isPresent())
                .forEach(
                        localEntityState -> {
                            PositionComponent positionComponentLocal =
                                    localEntityState.fetch(PositionComponent.class).orElseThrow();

                            multiplayerManager
                                    .entityStream()
                                    .forEach(
                                            multiplayerEntityState -> {
                                                if (multiplayerEntityState.globalID()
                                                        == localEntityState.globalID()) {
                                                    PositionComponent positionComponentMultiplayer =
                                                            (PositionComponent)
                                                                    multiplayerEntityState
                                                                            .fetch(
                                                                                    PositionComponent
                                                                                            .class)
                                                                            .orElseThrow();
                                                    positionComponentLocal.position(
                                                            positionComponentMultiplayer
                                                                    .position());
                                                }
                                            });
                        });
    }

    /**
     * Synchronizes animation of entities that are handled on other clients.
     *
     * <p>It is needed for entities which are not handled on own device, like the animation of a
     * hero played by another client.
     *
     * <p>Animation update follows as in {@link core.systems.VelocitySystem}
     */
    private void synchronizeAnimation() {
        Game.entityStream()
                .filter(entity -> entity.fetch(VelocityComponent.class).isPresent())
                .forEach(
                        localEntityState ->
                                multiplayerManager
                                        .entityStream()
                                        .forEach(
                                                multiplayerEntityState -> {
                                                    if (multiplayerEntityState.globalID()
                                                            == localEntityState.globalID()) {
                                                        DrawComponent drawComponent =
                                                                (DrawComponent)
                                                                        localEntityState
                                                                                .fetch(
                                                                                        DrawComponent
                                                                                                .class)
                                                                                .orElseThrow();

                                                        VelocityComponent
                                                                velocityComponentMultiplayer =
                                                                        multiplayerEntityState
                                                                                .fetch(
                                                                                        VelocityComponent
                                                                                                .class)
                                                                                .orElseThrow();

                                                        float x =
                                                                velocityComponentMultiplayer
                                                                        .currentXVelocity();
                                                        if (x > 0) {
                                                            drawComponent.currentAnimation(
                                                                    CoreAnimations.RUN_RIGHT);
                                                        } else if (x < 0) {
                                                            drawComponent.currentAnimation(
                                                                    CoreAnimations.RUN_LEFT);
                                                        }
                                                        // idle
                                                        else {
                                                            // each draw component has an idle
                                                            // animation, so no check is needed
                                                            if (drawComponent.isCurrentAnimation(
                                                                            CoreAnimations
                                                                                    .IDLE_LEFT)
                                                                    || drawComponent
                                                                            .isCurrentAnimation(
                                                                                    CoreAnimations
                                                                                            .RUN_LEFT))
                                                                drawComponent.currentAnimation(
                                                                        CoreAnimations.IDLE_LEFT);
                                                            else
                                                                drawComponent.currentAnimation(
                                                                        CoreAnimations.IDLE_RIGHT);
                                                        }
                                                    }
                                                }));
    }

    /** Removes all entities that has been marked as multiplayer entity. */
    private void removeMultiplayerEntities() {
        multiplayerManager
                .entityStream()
                .forEach(
                        globalEntity -> {
                            Game.entityStream()
                                    .filter(x -> x.globalID() == globalEntity.globalID())
                                    .findFirst()
                                    .ifPresent(Game::removeEntity);
                        });
    }
}
