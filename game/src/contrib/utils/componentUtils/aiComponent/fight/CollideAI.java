package contrib.utils.componentUtils.aiComponent.fight;

import core.Entity;
import core.level.Tile;
import core.utils.Constants;
import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.componentUtils.aiComponent.AITools;
import contrib.utils.componentUtils.aiComponent.IFightAI;

public class CollideAI implements IFightAI {
    private final float rushRange;
    private final int delay = Constants.FRAME_RATE;
    private int timeSinceLastUpdate = delay;
    private GraphPath<Tile> path;

    /**
     * Attacks the player by colliding if he is within the given range. Otherwise, it will move
     * towards the player.
     *
     * @param rushRange Range in which the faster collide logic should be executed
     */
    public CollideAI(float rushRange) {
        this.rushRange = rushRange;
    }

    @Override
    public void fight(Entity entity) {
        if (AITools.playerInRange(entity, rushRange)) {
            // the faster pathing once a certain range is reached
            path = AITools.calculatePathToHero(entity);
            AITools.move(entity, path);
            timeSinceLastUpdate = delay;
        } else {
            // check if new pathing update
            if (timeSinceLastUpdate >= delay) {
                path = AITools.calculatePathToHero(entity);
                timeSinceLastUpdate = -1;
            }
            timeSinceLastUpdate++;
            AITools.move(entity, path);
        }
    }
}
