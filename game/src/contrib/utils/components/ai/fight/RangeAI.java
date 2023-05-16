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

import static com.badlogic.gdx.math.MathUtils.random;
import static contrib.utils.components.ai.AITools.getAccessibleTilesInRange;


public class RangeAI implements IFightAI {

    private final float attackRange;
    private final float distance;
    private final Skill skill;
    boolean isNotBetweenAttackAndDistance = true;
    private GraphPath<Tile> path;

    /**
     * Attacks the player if he is within the given range between attackRange and distance. Otherwise, it will move into that range.
     *
     * @param attackRange max. Range in which the attack skill should be executed
     * @param distance min. Range in which the attack skill should be executed
     * @param skill Skill to be used when an attack is performed
     */

    public RangeAI(float attackRange, float distance, Skill skill){
        this.attackRange = attackRange;
        this.distance = distance;
        this.skill = skill;
    }

    @Override
    public void fight(Entity entity) {
        if(attackRange > distance) {
            if (AITools.playerInRange(entity, distance) && isNotBetweenAttackAndDistance) {
                isNotBetweenAttackAndDistance = false;
                Point positionHero = getPosition(Game.getHero().get());
                Point positionEntity = getPosition(entity);
                List<Tile> tiles = getAccessibleTilesInRange(positionEntity, attackRange - distance);
                Point newPosition;
                boolean newPositionFound = false;
                for (int index = 0; index < tiles.size(); index++) {
                    newPosition = tiles.get(index).getCoordinate().toPoint();
                    if (!AITools.inRange(newPosition, positionHero, distance)) {
                        path = AITools.calculatePath(positionEntity, newPosition);
                        newPositionFound = true;
                        break;
                    }
                }
                if (!newPositionFound) {
                    tiles = getAccessibleTilesInRange(positionHero, distance);
                    newPosition = tiles.get(random.nextInt(tiles.size())).getCoordinate().toPoint();
                    path = AITools.calculatePath(positionEntity, newPosition);
                }
            }

            if (!AITools.playerInRange(entity, attackRange) && isNotBetweenAttackAndDistance) {
                isNotBetweenAttackAndDistance = false;
                path = AITools.calculatePathToHero(entity);
            }

            if (!(AITools.playerInRange(entity, attackRange) && !AITools.playerInRange(entity, distance))) {
                AITools.move(entity, path);
                isNotBetweenAttackAndDistance = true;
            } else {
                skill.execute(entity);
            }
        }
    }

    private Point getPosition(Entity entity){
        return ((PositionComponent)
            entity.getComponent(PositionComponent.class)
                .orElseThrow(
                    () ->
                        new MissingComponentException(
                            "PositionComponent")))
            .getPosition();
    }
}
