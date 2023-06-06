package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import starter.Game;
import tools.Point;

/**
 * This GhostWalk is used to let the Npc walk randomly.
 *
 * The NpcWalk is a walk, that randomly decide between follow the Player, walk
 * random or despawn.
 */
public class GhostWalk implements IIdleAI {

    private GraphPath<Tile> path;

    /**
     * The Methode randomly changes the path of the Entity.
     * <p>
     * The follows the player, randomly walks through the map or despawns.
     *
     * @param entity is the Entity, which is walking.
     */
    @Override
    public void idle(Entity entity) {
        if (path == null) {
            path = AITools.calculatePathToHero(entity);
        }
        if (!AITools.playerInRange(entity, 2) && !AITools.pathFinishedOrLeft(entity, path)) {
            AITools.move(entity, path);
            return;
        }
        int randomPath = (int) (Math.random() * 1000);
        final int GO_TO_PLAYER = 990;
        final int DISAPPEAR = 0;
        if (randomPath < GO_TO_PLAYER && !(AITools.playerInRange(entity, 2))) {
            path = AITools.calculatePathToHero(entity);
        } else if (!(randomPath < GO_TO_PLAYER)) {
            path = AITools.calculatePathToRandomTileInRange(entity, 40f);
            return;
        } else if (randomPath == DISAPPEAR) {
            Game.removeEntity(entity);
        }
    }
}
