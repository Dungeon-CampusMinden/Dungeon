package contrib.utils.components.ai.idle;

import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.path.TilePath;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.Time;
import core.utils.components.MissingComponentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Implements an idle AI that lets the entity walk a specific path.
 *
 * <p>There are different modes. The entity can walk to random checkpoints, looping the same path or
 * walking the path back and forth.
 */
public final class PatrolWalk implements Consumer<Entity> {

  private static final Random RANDOM = new Random();

  private final List<Tile> checkpoints = new ArrayList<>();
  private final int numberCheckpoints;
  private final long maxPauseTimeMs;
  private final float radius;
  private final MODE mode;

  private TilePath currentPath;
  private boolean initialized = false;
  private boolean forward = true;
  private int currentCheckpoint = 0;
  private long waitStartedAtMs = Long.MIN_VALUE;
  private long currentPauseDurationMs = 0L;

  /**
   * Walks a random pattern in a radius around the entity.
   *
   * <p>The checkpoints will be chosen randomly at first idle. After being initialized, the
   * checkpoints won't change anymore, only the order may be.
   *
   * @param radius max distance from the entity to walk
   * @param numberCheckpoints number of checkpoints to walk to
   * @param pauseTime max time in milliseconds to wait on a checkpoint. The actual pause duration is
   *     chosen randomly between {@code 0} and this value.
   * @param mode patrol mode
   */
  public PatrolWalk(float radius, int numberCheckpoints, int pauseTime, final MODE mode) {
    this.radius = radius;
    this.numberCheckpoints = numberCheckpoints;
    this.maxPauseTimeMs = Math.max(0L, pauseTime);
    this.mode = mode;
  }

  private void init(final Entity entity) {
    initialized = true;

    PositionComponent position =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    Point center = position.position();
    Tile tile = core.Game.tileAt(position.position()).orElse(null);

    if (tile == null) {
      return;
    }

    List<Tile> accessibleTiles = LevelUtils.accessibleTilesInRange(center, radius);
    int maxTries = 0;
    while (this.checkpoints.size() < this.numberCheckpoints && maxTries < 1000) {
      Tile t = accessibleTiles.get(RANDOM.nextInt(accessibleTiles.size()));
      if (!this.checkpoints.contains(t)) {
        this.checkpoints.add(t);
      }
      maxTries++;
    }
  }

  @Override
  public void accept(final Entity entity) {
    if (!initialized) {
      this.init(entity);
    }

    if (this.checkpoints.isEmpty()) {
      initialized = false;
      return;
    }

    PositionComponent position =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    if (currentPath != null && !AIUtils.pathFinished(entity, currentPath)) {
      if (AIUtils.pathLeft(entity, currentPath)) {
        currentPath =
            LevelUtils.calculateTilePath(
                position.position(), this.checkpoints.get(currentCheckpoint).position());
      }
      AIUtils.followPath(entity, currentPath);
      return;
    }

    if (currentPath != null && AIUtils.pathFinished(entity, currentPath)) {
      currentPath = null;
      beginPause();
      return;
    }

    if (isWaiting()) {
      if (!pauseFinished()) {
        return;
      }
      endPause();
    }

    selectNextCheckpoint();
    currentPath =
        LevelUtils.calculateTilePath(
            position.position(), this.checkpoints.get(currentCheckpoint).position());
  }

  private void beginPause() {
    waitStartedAtMs = Time.nowMs();
    currentPauseDurationMs = maxPauseTimeMs <= 0L ? 0L : RANDOM.nextLong(maxPauseTimeMs + 1L);
  }

  private boolean isWaiting() {
    return waitStartedAtMs != Long.MIN_VALUE;
  }

  private boolean pauseFinished() {
    return Time.sinceMs(waitStartedAtMs) >= currentPauseDurationMs;
  }

  private void endPause() {
    waitStartedAtMs = Long.MIN_VALUE;
    currentPauseDurationMs = 0L;
  }

  private void selectNextCheckpoint() {
    switch (mode) {
      case RANDOM -> currentCheckpoint = RANDOM.nextInt(checkpoints.size());
      case LOOP -> currentCheckpoint = (currentCheckpoint + 1) % checkpoints.size();
      case BACK_AND_FORTH -> {
        if (forward) {
          currentCheckpoint += 1;
          if (currentCheckpoint == checkpoints.size()) {
            forward = false;
            currentCheckpoint = checkpoints.size() - 2;
          }
        } else {
          currentCheckpoint -= 1;
          if (currentCheckpoint == -1) {
            forward = true;
            currentCheckpoint = 1;
          }
        }
      }
      default -> {}
    }
  }

  /** WTF? . */
  public enum MODE {
    /** Walks to a random checkpoint. */
    RANDOM,

    /** Looping the same path over and over again. */
    LOOP,

    /** Walks the path forward and then backward. */
    BACK_AND_FORTH
  }
}
