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
      AIUtils.move(entity, currentPath);
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
    BACK_AND_FORTH
  }
}
