package contrib.modules.levelHide;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Rectangle;

/** Factory class for creating level hide entities. */
public class LevelHideFactory {

  /**
   * Creates a level hide entity.
   *
   * @param bottomLeft the bottom left point of the hide region
   * @param width width of the hide region
   * @param height height of the hide region
   * @param transitionSize the size of the transition area
   * @return the created level hide entity
   */
  public static Entity createLevelHide(
      Point bottomLeft, float width, float height, float transitionSize) {
    Entity entity = new Entity("hider");
    entity.add(new PositionComponent(bottomLeft));
    entity.add(
        new LevelHideComponent(
            new Rectangle(width, height, bottomLeft.x(), bottomLeft.y()), transitionSize));
    return entity;
  }

  /**
   * Creates a level hide entity with a default transition size of 2.
   *
   * @param bottomLeft the bottom left point of the hide region
   * @param width width of the hide region
   * @param height height of the hide region
   * @return the created level hide entity
   */
  public static Entity createLevelHide(Point bottomLeft, float width, float height) {
    return createLevelHide(bottomLeft, width, height, 2);
  }
}
