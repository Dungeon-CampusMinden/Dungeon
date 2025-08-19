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
import java.util.Random;
import java.util.function.Consumer;

/**
<<<<<<< HEAD
 * Implements an idle AI that lets the entity walk a specific path.
 *
 * <p>There are different modes. The entity can walk to random checkpoints, looping the same path or
 * walking the path back and forth.
=======
 * Implements an idle AI behavior allowing an entity to patrol between tiles on a map.
 *
 * <p>The patrol area is determined by a circular radius around the entity's current position. At
 * initialization, a set of unique, accessible checkpoints is selected within this radius. The
 * entity then moves between these checkpoints using one of the following patrol modes:
 *
 * <ul>
 *   <li>{@code RANDOM}: Move to a randomly chosen checkpoint each time.
 *   <li>{@code LOOP}: Move sequentially through all checkpoints and repeat from the beginning.
 *   <li>{@code BACK_AND_FORTH}: Move forward through the list, then reverse direction upon reaching
 *       the end.
 * </ul>
 *
 * <p>The AI also includes a configurable pause time at each checkpoint before proceeding to the
 * next.
>>>>>>> 93a40dbe (Apply spotless formatting)
 */
public final class PatrolWalk implements Consumer<Entity> {

  private static final Random RANDOM = new Random();
  private final List<Tile> checkpoints = new ArrayList<>();
  private final int numberCheckpoints;
  private final int pauseFrames;
  private final float radius;
  private final MODE mode;
<<<<<<< HEAD
  private GraphPath<Tile> currentPath;
  private boolean initialized = false;
  private boolean forward = true;
  private int frameCounter = -1;
  private int currentCheckpoint = 0;

  /**
   * WTF? (erster Satz kurze Beschreibung) .
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

  private void init(final Entity entity) {
    initialized = true;
    PositionComponent position =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Point center = position.position();
    Tile tile = Game.tileAT(position.position());

    if (tile == null) {
      return;
    }

    List<Tile> accessibleTiles = LevelUtils.accessibleTilesInRange(center, radius);

    if (accessibleTiles.isEmpty()) {
      return;
    }

    int maxTries = 0;
    while (this.checkpoints.size() < numberCheckpoints
        || accessibleTiles.size() == this.checkpoints.size()
        || maxTries >= 1000) {
      Tile t = accessibleTiles.get(RANDOM.nextInt(accessibleTiles.size()));
      if (!this.checkpoints.contains(t)) {
        this.checkpoints.add(t);
      }
      maxTries++;
    }
  }

  @Override
  public void accept(final Entity entity) {
    if (!initialized) this.init(entity);
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
            LevelUtils.calculatePath(
                position.position(), this.checkpoints.get(currentCheckpoint).position());
      }
      AIUtils.followPath(entity, currentPath);
      return;
    }

    if (currentPath != null && AIUtils.pathFinished(entity, currentPath)) {
      frameCounter = 0;
      currentPath = null;
      return;
    }

    if (frameCounter++ < pauseFrames && frameCounter != -1) {
      return;
    }

    // HERE: (Path to checkpoint finished + pause time over) OR currentPath = null
    this.frameCounter = -1;

    switch (mode) {
      case RANDOM -> {
        Random rnd = new Random();
        currentCheckpoint = rnd.nextInt(checkpoints.size());
        currentPath =
            LevelUtils.calculatePath(
                position.position(), this.checkpoints.get(currentCheckpoint).position());
      }
      case LOOP -> {
        currentCheckpoint = (currentCheckpoint + 1) % checkpoints.size();
        currentPath =
            LevelUtils.calculatePath(
                position.position(), this.checkpoints.get(currentCheckpoint).position());
      }
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
        currentPath =
            LevelUtils.calculatePath(
                position.position(), this.checkpoints.get(currentCheckpoint).position());
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
=======

  private GraphPath<Tile> currentPath;
  private boolean initialized = false;
  private boolean forward = true;
  private int frameCounter = -1;
  private int currentCheckpoint = 0;

  /**
   * Constructs a new PatrolWalk behavior instance.
   *
   * <p>This idle AI behavior moves an entity between a set of accessible checkpoints within a
   * defined radius around its initial position. The movement pattern depends on the selected patrol
   * mode and includes a randomized pause at each checkpoint.
   *
   * @param radius The maximum distance (in tiles) from the entity's position used to select
   *     accessible checkpoint tiles.
   * @param numberCheckpoints The number of unique checkpoints to generate within the radius.
   * @param pauseTimeMillis The maximum wait time (in milliseconds) at each checkpoint. The actual
   *     pause duration is randomly selected between 0 and this value.
   * @param mode The patrol mode defining the movement pattern (e.g., RANDOM, LOOP, BACK_AND_FORTH).
   */
  public PatrolWalk(float radius, int numberCheckpoints, int pauseTimeMillis, final MODE mode) {
    this.radius = radius;
    this.numberCheckpoints = numberCheckpoints;
    this.pauseFrames = pauseTimeMillis / (1000 / Game.frameRate());
    this.mode = mode;
  }

  /**
   * Initializes a list of accessible patrol checkpoints around the entity's current position.
   *
   * <p>This method attempts to retrieve the PositionComponent of the entity. It checks whether the
   * entity is standing on a valid tile, collects accessible tiles within a defined radius, and
   * randomly selects unique patrol checkpoints.
   *
   * @param entity The entity around whose position patrol checkpoints are generated.
   * @return {@code true} if the PositionComponent is present and at least one valid checkpoint was
   *     found within maxTries attempts, {@code false} otherwise.
   */
  private boolean initializeCheckpoints(final Entity entity) {
    initialized = true;

    Optional<PositionComponent> positionOpt = entity.fetch(PositionComponent.class);
    if (positionOpt.isEmpty()) return false;

    Point center = positionOpt.get().position();

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

    return !checkpoints.isEmpty();
  }

  /**
   * Handles initialization, movement along a path.
   *
   * @param entity The entity for which the behavior is performed.
   */
  @Override
  public void accept(final Entity entity) {
    if (!initializeIfNeeded(entity) || !hasValidCheckpoints()) return;

    PositionComponent position =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    if (currentPath != null && !AIUtils.pathFinished(entity, currentPath)) {
      if (AIUtils.pathLeft(entity, currentPath)) {
        currentPath =
            LevelUtils.calculatePath(
                position.position(), this.checkpoints.get(currentCheckpoint).position());
      }
      AIUtils.followPath(entity, currentPath);
      return;
    }

    if (handleFinishedPath(entity)) return;
    if (frameCounter++ < pauseFrames && frameCounter != -1) return;

    frameCounter = -1;
    advanceToNextCheckpoint(position);
  }

  /**
   * Ensures checkpoints are initialized if not yet initialized.
   *
   * @param entity The entity used to initialize the checkpoints.
   * @return true if initialization succeeded or already completed.
   */
  private boolean initializeIfNeeded(Entity entity) {
    if (!initialized) {
      initialized = initializeCheckpoints(entity);
    }
    return initialized;
  }

  /**
   * Checks whether valid checkpoints are available.
   *
   * @return true if at least one checkpoint is available.
   */
  private boolean hasValidCheckpoints() {
    if (checkpoints.isEmpty()) {
      initialized = false;
      return false;
    }
    return true;
  }

  /**
   * Handles actions when the current path has just been completed.
   *
   * @param entity The target entity.
   * @return true if path was completed and pause is initiated.
   */
  private boolean handleFinishedPath(Entity entity) {
    if (currentPath != null && AIUtils.pathFinished(entity, currentPath)) {
      frameCounter = 0;
      currentPath = null;
      return true;
    }
    return false;
  }

  /**
   * Determines the next checkpoint based on patrol mode and calculates a path to it.
   *
   * @param position The current position component of the entity.
   */
  private void advanceToNextCheckpoint(PositionComponent position) {
    switch (mode) {
      case RANDOM -> currentCheckpoint = RANDOM.nextInt(checkpoints.size());
      case LOOP -> currentCheckpoint = (currentCheckpoint + 1) % checkpoints.size();
      case BACK_AND_FORTH -> updateCheckpointBackAndForth();
    }

    currentPath =
        LevelUtils.calculatePath(
            position.position(), checkpoints.get(currentCheckpoint).position());
  }

  /**
   * Updates the checkpoint index for BACK_AND_FORTH mode. Reverses direction when reaching either
   * end of the checkpoint list.
   */
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

  /** Enum representing the patrol mode to use when selecting the next checkpoint. */
  public enum MODE {
    /** Select a random checkpoint. */
    RANDOM,

    /** Cycle through checkpoints sequentially. */
    LOOP,

    /** Move forward through checkpoints and then reverse back. */
>>>>>>> 93a40dbe (Apply spotless formatting)
    BACK_AND_FORTH
  }
}
