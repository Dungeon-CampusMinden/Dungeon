package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import starter.Game;
import tools.Point;

public class GhostWalk implements IIdleAI {

    private int randomPath = -1;
    private GraphPath<Tile> path;

    /**The ghost randomly changes its path.
     * He follows the player,randomly walks through the map or despawns*/

    @Override
    public void idle(Entity entity) {
        if(randomPath == -1){
            path = AITools.calculatePathToHero(entity);
        }
        if(AITools.playerInRange(entity, 2) || AITools.pathFinishedOrLeft(entity, path)){
            randomPath = (int) (Math.random() * 1000);
            if(randomPath > 9 && !(AITools.playerInRange(entity, 2))){
                path = AITools.calculatePathToHero(entity);
            }
            else if(randomPath < 10){
                path = AITools.calculatePathToRandomTileInRange(entity, 40f);
            }
            if(randomPath == 999){
                Game.removeEntity(entity);
            }
        }
        AITools.move(entity, path);
    }
}
