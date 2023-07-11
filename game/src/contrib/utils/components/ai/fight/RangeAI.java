package contrib.utils.components.ai.fight;

import static contrib.utils.components.ai.AITools.accessibleTilesInRange;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AITools;
import contrib.utils.components.skill.Skill;

import core.Entity;
import core.Game;
import core.level.Tile;
import core.utils.Point;

import java.util.List;
import java.util.function.Consumer;

public final class RangeAI implements Consumer<Entity> {

    private final float attackRange;
    private final float distance;
    private final Skill skill;
    private GraphPath<Tile> path;

    /**
     * Attacks the player if he is within the given range between attackRange and distance.
     * Otherwise, it will move into that range.
     *
     * @param attackRange max. Range in which the attack skill should be executed
     * @param distance min. Range in which the attack skill should be executed
     * @param skill Skill to be used when an attack is performed
     */
    public RangeAI(final float attackRange, final float distance, final Skill skill) {
        if (attackRange <= distance || distance < 0) {
            throw new Error(
                    "attackRange must be greater than distance and distance must be 0 or greater than 0");
        }
        if (Game.hero().isEmpty()) {
            throw new Error("There must be a Hero in the Game!");
        }
        this.attackRange = attackRange;
        this.distance = distance;
        this.skill = skill;
    }

    @Override
    public void accept(final Entity entity) {
        boolean playerInDistanceRange = AITools.playerInRange(entity, distance);
        boolean playerInAttackRange = AITools.playerInRange(entity, attackRange);

        if (playerInAttackRange) {
            if (playerInDistanceRange) {
                Point positionHero = Game.positionOf(Game.hero().orElseThrow());
                Point positionEntity = Game.positionOf(entity);
                List<Tile> tiles = accessibleTilesInRange(positionEntity, attackRange - distance);
                boolean newPositionFound = false;
                for (Tile tile : tiles) {
                    Point newPosition = tile.position();
                    if (!Point.inRange(newPosition, positionHero, distance)) {
                        path = AITools.calculatePath(positionEntity, newPosition);
                        newPositionFound = true;
                        break;
                    }
                }
                if (!newPositionFound) {
                    path = AITools.calculatePathToRandomTileInRange(entity, 2 * attackRange);
                }
                AITools.move(entity, path);
            } else {
                skill.execute(entity);
            }
        } else {
            path = AITools.calculatePathToHero(entity);
            AITools.move(entity, path);
        }
    }
}
