package feature.timer;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;

/** Factory for creating world timer entities. */
public class WorldTimerFactory {

  /**
   * Create a world timer entity with the given position, play-clock start, and duration.
   *
   * @param pos the position of the world timer entity
   * @param startedAtActiveMs active play time when the countdown began
   * @param duration the duration of the timer in seconds
   * @return a new world timer entity with the specified position, timestamp, and duration
   */
  public static Entity createWorldTimer(Point pos, long startedAtActiveMs, int duration) {
    Entity e = new Entity();
    e.add(new PositionComponent(pos));
    e.add(new WorldTimerComponent(startedAtActiveMs, duration));
    e.add(new DrawComponent(new SimpleIPath("animation/missing_texture.png")));
    return e;
  }
}
