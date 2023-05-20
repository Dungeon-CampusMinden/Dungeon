package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AITools;
import contrib.utils.components.ai.IFightAI;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.List;

import static contrib.utils.components.ai.AITools.getAccessibleTilesInRange;

public class RangeAI implements IFightAI {

    private final float attackRange;
    private final float distance;
    private final Skill skill;
    private GraphPath<Tile> path;

    /**
     * Attacks the player if he is within the given range between attackRange and distance. Otherwise, it will move into that range.
     *
     * @param attackRange max. Range in which the attack skill should be executed
     * @param distance min. Range in which the attack skill should be executed
     * @param skill Skill to be used when an attack is performed
     */

    public RangeAI(float attackRange, float distance, Skill skill){
        if(attackRange <= distance || distance < 0){
            throw new Error("attackRange must be greater than distance and distance must be 0 or greater than 0");
        }
        if(Game.getHero().isEmpty()){
            throw new Error("There must be a Hero in the Game!");
        }
        this.attackRange = attackRange;
        this.distance = distance;
        this.skill = skill;
    }

    @Override
    public void fight(Entity entity) {
        boolean playerInDistanceRange = AITools.playerInRange(entity, distance);
        boolean playerInAttackRange = AITools.playerInRange(entity, attackRange);

        if (playerInAttackRange) {
            if(playerInDistanceRange) {
                Point positionHero = getPosition(Game.getHero().orElseThrow());
                Point positionEntity = getPosition(entity);
                List<Tile> tiles = getAccessibleTilesInRange(positionEntity, attackRange - distance);
                boolean newPositionFound = false;
                for (Tile tile : tiles) {
                    Point newPosition = tile.getCoordinate().toPoint();
                    if (!AITools.inRange(newPosition, positionHero, distance)) {
                        path = AITools.calculatePath(positionEntity, newPosition);
                        newPositionFound = true;
                        break;
                    }
                }
                if (!newPositionFound) {
                    path = AITools.calculatePathToRandomTileInRange(entity, 2 * attackRange);
                }
                AITools.move(entity, path);
            }
            else{
                skill.execute(entity);
            }
        }
        else {
            path = AITools.calculatePathToHero(entity);
            AITools.move(entity, path);
        }
    }


    private Point getPosition(Entity entity) {
        return ((PositionComponent)
            entity.getComponent(PositionComponent.class)
                .orElseThrow(
                    () ->
                        new MissingComponentException(
                            entity.getClass().getName() + "is missing PositionComponent")))
            .getPosition();
    }
}
