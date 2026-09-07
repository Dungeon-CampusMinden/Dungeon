package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;

public class EntityFactory {

  public static Entity cryoBox(Point point, boolean on) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    if (on) entity.add(new DrawComponent(new SimpleIPath("objects/tech/CryoBox.png")));
    else entity.add(new DrawComponent(new SimpleIPath("objects/tech/CryoBoxOFF.png")));

    return entity;
  }
}
