package contrib.modules.levelHide;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Rectangle;

public class LevelHideFactory {

  public static Entity createLevelHide(Point bottomLeft, float width, float height) {
    Entity entity = new Entity("hider");
    entity.add(new PositionComponent(bottomLeft));
    entity.add(
        new LevelHideComponent(new Rectangle(width, height, bottomLeft.x(), bottomLeft.y()), 2));
    return entity;
  }
}
