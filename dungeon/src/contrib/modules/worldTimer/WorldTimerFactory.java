package contrib.modules.worldTimer;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;

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
    return e;
  }
}
