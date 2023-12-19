package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelUtils;
import java.util.function.Consumer;

/**
 * Implements a fight AI. The entity attacks the player if the player is colliding with the entity.
 */
public class CollideAI implements Consumer<Entity> {
  private final float rushRange;
  private final int delay = Game.frameRate();
  private int timeSinceLastUpdate = delay;
  private GraphPath<Tile> path;

  /**
   * Attacks the player by colliding if he is within the given range. Otherwise, it will move
   * towards the player.
   *
   * @param rushRange Range in which the faster collide logic should be executed.
   */
  public CollideAI(final float rushRange) {
    this.rushRange = rushRange;
  }

  @Override
  public void accept(final Entity entity) {
    if (LevelUtils.playerInRange(entity, rushRange)) {
      // the faster pathing once a certain range is reached
      path = LevelUtils.calculatePathToHero(entity);
      AIUtils.move(entity, path);
      timeSinceLastUpdate = delay;
    } else {
      // check if new pathing update
      if (timeSinceLastUpdate >= delay) {
        path = LevelUtils.calculatePathToHero(entity);
        timeSinceLastUpdate = -1;
      }
      timeSinceLastUpdate++;
      AIUtils.move(entity, path);
    }
  }
}
