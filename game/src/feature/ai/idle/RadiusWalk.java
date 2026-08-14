package feature.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import engine.Entity;
import engine.Game;
import engine.level.Tile;
import engine.level.utils.LevelUtils;
import feature.ai.AIUtils;
import java.util.function.Consumer;

/**
 * Implements an idle AI that lets the entity walk in a specific radius from its current position.
 */
public final class RadiusWalk implements Consumer<Entity> {
  private final float radius;
  private final int breakTime;
  private GraphPath<Tile> path;
  private int currentBreak = 0;

  /**
   * Finds a point in the radius and then moves there. When the point has been reached, a new point
   * in the radius is searched for from there.
   *
   * @param radius Radius in which a target point is to be searched for.
   * @param breakTimeInSeconds How long to wait (in seconds) before searching a new goal.
   */
  public RadiusWalk(float radius, int breakTimeInSeconds) {
    this.radius = radius;
    this.breakTime = breakTimeInSeconds * Game.frameRate();
  }

  @Override
  public void accept(final Entity entity) {
    if (path == null || AIUtils.pathFinishedOrLeft(entity, path)) {
      if (currentBreak >= breakTime) {
        currentBreak = 0;
        path = LevelUtils.calculatePathToRandomTileInRange(entity, radius);
        accept(entity);
      }

      currentBreak++;

    } else AIUtils.followPath(entity, path);
  }
}
