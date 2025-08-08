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
 * Implements an idle AI behavior allowing an entity to patrol between tiles on a map.
 * <p>
 * The patrol area is determined by a circular radius around the entity's current position.
 * At initialization, a set of unique, accessible checkpoints is selected within this radius.
 * The entity then moves between these checkpoints using one of the following patrol modes:
 * <ul>
 *   <li>{@code RANDOM}: Move to a randomly chosen checkpoint each time.</li>
 *   <li>{@code LOOP}: Move sequentially through all checkpoints and repeat from the beginning.</li>
 *   <li>{@code BACK_AND_FORTH}: Move forward through the list, then reverse direction upon reaching the end.</li>
 * </ul>
 * <p>
 * The AI also includes a configurable pause time at each checkpoint before proceeding to the next.
 */
public final class PatrolWalk implements Consumer<Entity> {

  private static final Random RANDOM = new Random();
  private final List<Tile> checkpoints = new ArrayList<>();
  private final int numberCheckpoints;
  private final int pauseFrames;
  private final float radius;
  private final MODE mode;

  private GraphPath<Tile> currentPath;
  private boolean initialized = false;
  private boolean forward = true;
  private int frameCounter = -1;
  private int currentCheckpoint = 0;

  /**
   * Constructs a new PatrolWalk behavior instance.
   *
   * <p>This idle AI behavior moves an entity between a set of accessible checkpoints
   * within a defined radius around its initial position. The movement pattern depends
   * on the selected patrol mode and includes a randomized pause at each checkpoint.
   *
   * @param radius            The maximum distance (in tiles) from the entity's position
   *                          used to select accessible checkpoint tiles.
   * @param numberCheckpoints The number of unique checkpoints to generate within the radius.
   * @param pauseTimeMillis   The maximum wait time (in milliseconds) at each checkpoint.
   *                          The actual pause duration is randomly selected between 0 and this value.
   * @param mode              The patrol mode defining the movement pattern
   *                          (e.g., RANDOM, LOOP, BACK_AND_FORTH).
   */
  public PatrolWalk(float radius, int numberCheckpoints, int pauseTimeMillis, final MODE mode) {
    this.radius = radius;
    this.numberCheckpoints = numberCheckpoints;
    this.pauseFrames = pauseTimeMillis / (1000 / Game.frameRate());
    this.mode = mode;
  }

  /**
   * Initializes a list of accessible patrol checkpoints around the entity’s current position.
   *
   * <p>This method attempts to retrieve the PositionComponent of the entity.
   * It checks whether the entity is standing on a valid tile, collects accessible tiles
   * within a defined radius, and randomly selects unique patrol checkpoints.
   *
   * @param entity The entity around whose position patrol checkpoints are generated.
   * @return {@code true} if at least one valid checkpoint was found, {@code false} otherwise.
   * @throws MissingComponentException if the PositionComponent is missing from the entity.
   */
  private boolean initializeCheckpoints(final Entity entity) {
    // Mark initialization as started to avoid re-initializing later
    initialized = true;

    // Retrieve position of the entity
    PositionComponent position = entity.fetch(PositionComponent.class)
      .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Point center = position.position(); // Current position as Point(x, y)

    // Get the tile under the entity (may be empty if outside level)
    Optional<Tile> tileOpt = Game.tileAT(center);
    if (tileOpt.isEmpty()) return false; // Abort if entity stands on invalid tile

    // Get all accessible tiles around the position within a certain radius
    List<Tile> accessibleTiles = LevelUtils.accessibleTilesInRange(center, radius);
    if (accessibleTiles.isEmpty()) return false; // Abort if nothing reachable

    // Randomly select unique tiles as patrol checkpoints
    int maxTries = 0;
    while (checkpoints.size() < numberCheckpoints
      && accessibleTiles.size() != checkpoints.size()
      && maxTries < 1000) {

      // Pick a random tile from accessible candidates
      Tile t = accessibleTiles.get(RANDOM.nextInt(accessibleTiles.size()));

      // Only add it if not already part of the list (unique checkpoints)
      if (!checkpoints.contains(t)) {
        checkpoints.add(t);
      }

      maxTries++; // Prevent infinite loops
    }

    // Success if at least one checkpoint was found
    return true;
  }

  /**
   * Executes the AI behavior for a given entity. Handles initialization, movement along a path,
   * waiting at a checkpoint, and selecting the next destination based on patrol mode.
   *
   * @param entity The entity for which the behavior is performed.
   */
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
   * Retrieves the PositionComponent of an entity.
   *
   * @param entity The target entity.
   * @return The PositionComponent.
   */
  private PositionComponent getPositionComponent(Entity entity) {
    return entity.fetch(PositionComponent.class)
      .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
  }

  /**
   * Handles the current path if it's still in progress.
   *
   * @param entity   The entity moving along the path.
   * @param position The entity's position component.
   * @return true if the entity is currently moving on a path.
   */
  private boolean handleOngoingPath(Entity entity, PositionComponent position) {
    if (isPathInProgress(entity)) {
      updatePathIfRequired(entity, position);
      AIUtils.move(entity, currentPath);
      return true;
    }
    return false;
  }

  /**
   * Handles actions when the current path has just been completed.
   *
   * @param entity The target entity.
   * @return true if path was completed and pause is initiated.
   */
  private boolean handleFinishedPath(Entity entity) {
    if (pathJustFinished(entity)) {
      frameCounter = 0;
      currentPath = null;
      return true;
    }
    return false;
  }

  /**
   * Determines whether the entity should wait at a checkpoint.
   *
   * @return true if the entity is still within the waiting time.
   */
  private boolean handleCheckpointWait() {
    return frameCounter++ < pauseFrames && frameCounter != -1;
  }

  /**
   * Checks if the entity is still on the current path.
   *
   * @param entity The entity to evaluate.
   * @return true if path is still in progress.
   */
  private boolean isPathInProgress(Entity entity) {
    return currentPath != null && !AIUtils.pathFinished(entity, currentPath);
  }

  /**
   * Updates the path if the entity has deviated from it.
   *
   * @param entity   The target entity.
   * @param position The current position component of the entity.
   */
  private void updatePathIfRequired(Entity entity, PositionComponent position) {
    if (AIUtils.pathLeft(entity, currentPath)) {
      currentPath = LevelUtils.calculatePath(
        position.position(), checkpoints.get(currentCheckpoint).position());
    }
  }

  /**
   * Checks whether the entity has just completed a path.
   *
   * @param entity The target entity.
   * @return true if path is completed.
   */
  private boolean pathJustFinished(Entity entity) {
    return currentPath != null && AIUtils.pathFinished(entity, currentPath);
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

    currentPath = LevelUtils.calculatePath(
      position.position(), checkpoints.get(currentCheckpoint).position());
  }

  /**
   * Updates the checkpoint index for BACK_AND_FORTH mode.
   * Reverses direction when reaching either end of the checkpoint list.
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

  /**
   * Enum representing the patrol mode to use when selecting the next checkpoint.
   */
  public enum MODE {
    /** Select a random checkpoint. */
    RANDOM,

    /** Cycle through checkpoints sequentially. */
    LOOP,

    /** Move forward through checkpoints and then reverse back. */
    BACK_AND_FORTH
  }
}
