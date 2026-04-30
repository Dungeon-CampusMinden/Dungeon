package contrib.utils.components.ai.fight;

import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.level.path.TilePath;
import core.level.utils.LevelUtils;
import core.utils.Time;
import java.util.function.Consumer;

/**
 * AI behavior for entities that chase and attack the player.
 *
 * <p>The entity will attempt to move towards the player and attack, if the player is within a
 * specified range.
 *
 * <p>Otherwise, it will continue to follow the last calculated path towards the player.
 *
 * <p>The path refresh outside the direct chase range is time-based instead of frame-based so the
 * behavior stays stable across different hosts and frame rates.
 */
public class AIChaseBehaviour implements Consumer<Entity> {

  /**
   * Default interval in milliseconds after which the cached chase path is recalculated while the
   * player is not currently inside the direct chase range.
   */
  private static final long DEFAULT_REPATH_INTERVAL_MS = 1000L;

  private final float chaseRange;
  private final long repathIntervalMs;

  private long lastPathUpdateMs = Long.MIN_VALUE;
  private TilePath path;

  /**
   * Creates a new AIChaseBehaviour with the given chase range.
   *
   * <p>The cached path outside the direct chase range is refreshed once per second.
   *
   * @param chaseRange The distance within which the entity will attempt to chase the player.
   */
  public AIChaseBehaviour(final float chaseRange) {
    this(chaseRange, DEFAULT_REPATH_INTERVAL_MS);
  }

  /**
   * Creates a new AIChaseBehaviour with the given chase range and repath interval.
   *
   * @param chaseRange The distance within which the entity will attempt to chase the player.
   * @param repathIntervalMs Interval in milliseconds for recalculating the cached chase path when
   *     the player is currently outside the direct chase range.
   */
  public AIChaseBehaviour(final float chaseRange, final long repathIntervalMs) {
    this.chaseRange = chaseRange;
    this.repathIntervalMs = Math.max(0L, repathIntervalMs);
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
    refreshPath(entity);
    AIUtils.followPath(entity, path);
  }

  private void handlePlayerNotInChaseRange(final Entity entity) {
    refreshPathIfDue(entity);
    AIUtils.followPath(entity, path);
  }

  private void refreshPathIfDue(final Entity entity) {
    if (path == null || Time.elapsedTimeMs(lastPathUpdateMs) >= repathIntervalMs) {
      refreshPath(entity);
    }
  }

  private void refreshPath(final Entity entity) {
    path = LevelUtils.calculateTilePathToPlayer(entity);
    lastPathUpdateMs = Time.currentTimeMillis();
  }
}
