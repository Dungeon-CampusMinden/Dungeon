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
public class AIChaseBehaviour implements Consumer<Entity> {
  private final float chaseRange;
  private final int delay = Game.frameRate();
  private int timeSinceLastUpdate = delay;
  private GraphPath<Tile> path;

  /**
   * Attacks the player by colliding if he is within the given range. Otherwise, it will move
   * towards the player.
   *
   * @param chaseRange Range in which the faster collide logic should be executed.
   */
  public AIChaseBehaviour(final float chaseRange) {
    this.chaseRange = chaseRange;
  }

  @Override
  public void accept(final Entity entity) {
    if (LevelUtils.playerInRange(entity, chaseRange)) {
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
