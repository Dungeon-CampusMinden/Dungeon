package contrib.modules.worldTimer;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;

public class WorldTimerFactory {

  public static Entity createWorldTimer(Point pos, int timestamp, int duration) {
    Entity e = new Entity();
    e.add(new PositionComponent(pos));
    e.add(new WorldTimerComponent(timestamp, duration));
    return e;
  }

}
