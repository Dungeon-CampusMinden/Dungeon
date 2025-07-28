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
 * <p>The entity will attempt to move towards the player and attack, if the player is within a specified range.
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
   * @param chaseRange The distance within which the entity will attempt to attack the player directly.
   */
  public AIChaseBehaviour(final float chaseRange) {
    this.chaseRange = chaseRange;
  }

  @Override
  public void accept(final Entity entity) {
    updateChase(entity);
  }

  /**
   * Updates the chase behavior for the given entity.
   *
   * <p>If the player is within the specified chase range, recalculates the path and moves the entity towards the player,
   * returning true.
   *
   * <p>Otherwise, updates the path at a fixed interval and continues moving the entity along the last path,
   * returning false.
   *
   * @param entity The entity whose chase behavior should be updated.
   * @return true if the player is within chase range and the entity should attack, false otherwise.
   */
  public boolean updateChase(final Entity entity) {
    if (LevelUtils.playerInRange(entity, chaseRange)) {
      path = LevelUtils.calculatePathToHero(entity);
      AIUtils.move(entity, path);
      timeSinceLastUpdate = delay;
      return true;
    } else {
      if (timeSinceLastUpdate >= delay) {
        path = LevelUtils.calculatePathToHero(entity);
        timeSinceLastUpdate = -1;
      }
      timeSinceLastUpdate++;
      AIUtils.move(entity, path);
      return false;
    }
  }
}
