package ecs.systems;

import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.mp.MultiplayerComponent;
import ecs.entities.HeroDummy;
import mp.MultiplayerAPI;
import starter.Game;
import tools.Point;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/** Used to synchronize multiplayer session state with local client state. */
public class MultiplayerSynchronizationSystem extends ECS_System {

    private final MultiplayerAPI multiplayerAPI;

    /**
     * @param multiplayerAPI
     */
    public MultiplayerSynchronizationSystem(final MultiplayerAPI multiplayerAPI) {
        super();
        this.multiplayerAPI = requireNonNull(multiplayerAPI);
    }

    @Override
    public void update() {
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
            Game.getEntities().stream()
                .flatMap(e -> e.getComponent(MultiplayerComponent.class).stream())
                .map(component -> ((MultiplayerComponent)component).getPlayerId())
                .collect(Collectors.toSet());
        multiplayerAPI.getHeroPositionByPlayerId()
            .forEach((Integer playerId, Point position) -> {
                boolean isOwnHero = playerId == multiplayerAPI.getOwnPlayerId();
                if (!isOwnHero) {
                    boolean isHeroNewJoined = !currentLocalMultiplayerEntityIds.contains(playerId);
                    if(isHeroNewJoined) new HeroDummy(position, playerId);
                }
        });
    }

    /* Removes multiplayer entities from local state, that are no longer part of multiplayer session. */
    private void synchronizeRemovedEntities() {
        Game.getEntities().stream()
            .flatMap(entity -> entity.getComponent(MultiplayerComponent.class).stream())
            .map(multiplayerComponent -> (MultiplayerComponent) multiplayerComponent)
            .forEach(multiplayerComponent -> {
                boolean isOwnHero = multiplayerComponent.getPlayerId() == multiplayerAPI.getOwnPlayerId();
                boolean isEntityRemoved =
                    !multiplayerAPI.getHeroPositionByPlayerId().containsKey(multiplayerComponent.getPlayerId());
                if (!isOwnHero && isEntityRemoved) Game.removeEntity(multiplayerComponent.getEntity());
            });
    }

    // TODO: not synchronizing position - update velocity component instead for movement animation over velocity system
    /* Synchronizes local positions with positions from multiplayer session. */
    private void synchronizePositions() {
        Game.getEntities().stream()
            .flatMap(entity -> entity.getComponent(MultiplayerComponent.class).stream())
            .map(multiplayerComponent -> (MultiplayerComponent) multiplayerComponent)
            .forEach(multiplayerComponent -> {
                    PositionComponent positionComponent =
                        (PositionComponent) multiplayerComponent
                            .getEntity()
                            .getComponent(PositionComponent.class)
                            .orElseThrow();
                    Point currentPositionAtMultiplayerSession =
                        multiplayerAPI.getHeroPositionByPlayerId().get(multiplayerComponent.getPlayerId());
                    positionComponent.setPosition(currentPositionAtMultiplayerSession);
            });
    }

    /* Removes all entities that has been added due to multiplayer session from local state */
    private void removeMultiplayerEntities() {
        Game.getEntities().stream()
            .forEach(entity -> {
                if (entity.getComponent(MultiplayerComponent.class).isPresent()) {
                    Game.removeEntity(entity);
                }
            });
    }
}
