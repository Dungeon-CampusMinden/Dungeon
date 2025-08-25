package contrib.utils.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelUtils;
import java.util.function.Consumer;

/**
 * AI behavior for entities that chase and attack the player.
 *
 * <p>The entity will attempt to move towards the player and attack, if the player is within a
 * specified range.
 *
 * <p>Otherwise, it will continue to follow the last calculated path towards the player.
 */
public class AIChaseBehaviour implements Consumer<Entity> {
  private final float chaseRange;
  private final int delay = Game.frameRate();
  private int timeSinceLastUpdate = delay;
  private GraphPath<Tile> path;

  /**
   * Creates a new AIChaseBehaviour with the given chase range.
   *
   * @param chaseRange The distance within which the entity will attempt to chase the player.
   */
  public AIChaseBehaviour(final float chaseRange) {
    this.chaseRange = chaseRange;
  }

  @Override
  public void accept(final Entity entity) {
    if (LevelUtils.playerInRange(entity, chaseRange)) {
      handlePlayerInChaseRange(entity);
    } else {
      handlePlayerNotInChaseRange(entity);
    }
  }

  private void handlePlayerInChaseRange(final Entity entity) {
    path = LevelUtils.calculatePathToHero(entity);
    AIUtils.followPath(entity, path);
    timeSinceLastUpdate = delay;
  }

  private void handlePlayerNotInChaseRange(final Entity entity) {
    if (timeSinceLastUpdate >= delay) {
      path = LevelUtils.calculatePathToHero(entity);
      timeSinceLastUpdate = 0;
    }
    timeSinceLastUpdate++;
    AIUtils.followPath(entity, path);
  }
}
