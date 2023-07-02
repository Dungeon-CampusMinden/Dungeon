package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.utils.components.ai.AITools;

import core.Dungeon;
import core.Entity;
import core.level.Tile;

import java.util.function.Consumer;

public class CollideAI implements Consumer<Entity> {
    private final float rushRange;
    private final int delay = Dungeon.frameRate();
    private int timeSinceLastUpdate = delay;
    private GraphPath<Tile> path;

    /**
     * Attacks the player by colliding if he is within the given range. Otherwise, it will move
     * towards the player.
     *
     * @param rushRange Range in which the faster collide logic should be executed
     */
    public CollideAI(final float rushRange) {
        this.rushRange = rushRange;
    }

    @Override
    public void accept(final Entity entity) {
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

    public float getRushRange() {
        return rushRange;
    }
}
