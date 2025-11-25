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
    entity.add(new LevelHideComponent(new Rectangle(width, height), transitionSize));
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

  /**
   * Creates a level hide entity defined by bottom left and top right points with a default.
   *
   * @param bottomLeft the bottom left point of the hide region
   * @param topRight the top right point of the hide region
   * @return the created level hide entity
   */
  public static Entity createLevelHide(Point bottomLeft, Point topRight) {
    return createLevelHide(bottomLeft, topRight, 2);
  }

  public static Entity createLevelHide(Point bottomLeft, Point topRight, float transitionSize) {
    float width = topRight.x() - bottomLeft.x();
    float height = topRight.y() - bottomLeft.y();
    return createLevelHide(bottomLeft, width, height, transitionSize);
  }
}
