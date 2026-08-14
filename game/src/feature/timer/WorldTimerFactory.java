package feature.timer;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;

/** Factory for creating world timer entities. */
public class WorldTimerFactory {

  /**
   * Create a world timer entity with the given position, timestamp, and duration.
   *
   * @param pos the position of the world timer entity
   * @param timestamp the timestamp (UNIX) for the timer
   * @param duration the duration of the timer in seconds
   * @return a new world timer entity with the specified position, timestamp, and duration
   */
  public static Entity createWorldTimer(Point pos, int timestamp, int duration) {
    Entity e = new Entity();
    e.add(new PositionComponent(pos));
    e.add(new WorldTimerComponent(timestamp, duration));
    e.add(new DrawComponent(new SimpleIPath("animation/missing_texture.png")));
    return e;
  }
}
