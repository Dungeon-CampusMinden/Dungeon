package contrib.utils.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  private final int pauseFrames;
  private final float radius;
  private final MODE mode;

  private GraphPath<Tile> currentPath = null;
  private boolean initialized = false;
  private boolean forward = true;
  private int frameCounter = -1;
  private int currentCheckpoint = 0;

  /**
   * WTF? (erster Satz kurze Beschreibung).
   *
   * <p>Walks a random pattern in a radius around the entity. The checkpoints will be chosen
   * randomly at first idle. After being initialized, the checkpoints won't change anymore, only the
   * order may be.
   *
   * @param radius Max distance from the entity to walk.
   * @param numberCheckpoints Number of checkpoints to walk to.
   * @param pauseTime Max time in milliseconds to wait on a checkpoint. The actual time is a random
   *     number between 0 and this value.
   * @param mode WTF? .
   */
  public PatrolWalk(float radius, int numberCheckpoints, int pauseTime, final MODE mode) {
    this.radius = radius;
    this.numberCheckpoints = numberCheckpoints;
    this.pauseFrames = pauseTime / (1000 / Game.frameRate());
    this.mode = mode;
  }

  private boolean initializeCheckpoints(final Entity entity) {
    initialized = true;
    PositionComponent position =
      entity.fetch(PositionComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Point center = position.position();
    Optional<Tile> tileOpt = Optional.ofNullable(Game.tileAT(center));

    if (tileOpt.isEmpty()) return false;

    List<Tile> accessibleTiles = LevelUtils.accessibleTilesInRange(center, radius);
    if (accessibleTiles.isEmpty()) return false;

    int maxTries = 0;
    while (checkpoints.size() < numberCheckpoints
      && accessibleTiles.size() != checkpoints.size()
      && maxTries < 1000) {
      Tile t = accessibleTiles.get(RANDOM.nextInt(accessibleTiles.size()));
      if (!checkpoints.contains(t)) {
        checkpoints.add(t);
      }
      maxTries++;
    }
    return true;
  }

  @Override
  public void accept(final Entity entity) {
    if (!initializeIfNeeded(entity) || !hasValidCheckpoints()) return;

    PositionComponent position = getPositionComponent(entity);

    if (handleOngoingPath(entity, position)) return;
    if (handleFinishedPath(entity)) return;
    if (handleCheckpointWait()) return;

    frameCounter = -1;
    advanceToNextCheckpoint(position);
  }

  private boolean initializeIfNeeded(Entity entity) {
    if (!initialized) {
      initialized = initializeCheckpoints(entity);
    }
    return initialized;
  }

  private boolean hasValidCheckpoints() {
    if (checkpoints.isEmpty()) {
      initialized = false;
      return false;
    }
    return true;
  }

  private PositionComponent getPositionComponent(Entity entity) {
    return entity.fetch(PositionComponent.class)
      .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
  }

  private boolean handleOngoingPath(Entity entity, PositionComponent position) {
    if (isPathInProgress(entity)) {
      updatePathIfRequired(entity, position);
      AIUtils.move(entity, currentPath);
      return true;
    }
    return false;
  }

  private boolean handleFinishedPath(Entity entity) {
    if (pathJustFinished(entity)) {
      frameCounter = 0;
      currentPath = null;
      return true;
    }
    return false;
  }

  private boolean handleCheckpointWait() {
    return frameCounter++ < pauseFrames && frameCounter != -1;
  }

  private boolean isPathInProgress(Entity entity) {
    return currentPath != null && !AIUtils.pathFinished(entity, currentPath);
  }

  private void updatePathIfRequired(Entity entity, PositionComponent position) {
    if (AIUtils.pathLeft(entity, currentPath)) {
      currentPath = LevelUtils.calculatePath(
        position.position(), checkpoints.get(currentCheckpoint).position());
    }
  }

  private boolean pathJustFinished(Entity entity) {
    return currentPath != null && AIUtils.pathFinished(entity, currentPath);
  }

  private void advanceToNextCheckpoint(PositionComponent position) {
    switch (mode) {
      case RANDOM -> currentCheckpoint = RANDOM.nextInt(checkpoints.size());
      case LOOP -> currentCheckpoint = (currentCheckpoint + 1) % checkpoints.size();
      case BACK_AND_FORTH -> updateCheckpointBackAndForth();
    }

    currentPath = LevelUtils.calculatePath(
      position.position(), checkpoints.get(currentCheckpoint).position());
  }

  private void updateCheckpointBackAndForth() {
    if (forward) {
      currentCheckpoint++;
      if (currentCheckpoint == checkpoints.size()) {
        forward = false;
        currentCheckpoint = checkpoints.size() - 2;
      }
    } else {
      currentCheckpoint--;
      if (currentCheckpoint == -1) {
        forward = true;
        currentCheckpoint = 1;
      }
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
