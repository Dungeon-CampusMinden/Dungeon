package entities;

import components.BlockComponent;
import components.PushableComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** Factory class for creating miscellaneous game entities. */
public class MiscFactory {

  private static final IPath STONE = new SimpleIPath("objects/stone/stone.png");
  private static final float STONE_SPEED = 7.5f;

  /**
   * Creates a stone entity at the given position.
   *
   * <p>A Stone is blocking and pushable entity.
   *
   * @param position The initial position of the stone.
   * @return A new stone entity.
   */
  public static Entity stone(Point position) {
    Entity stone = new Entity("stone");
    stone.add(new BlockComponent());
    stone.add(new PushableComponent());
    stone.add(new PositionComponent(position.toCoordinate().toCenteredPoint()));
    stone.add(new VelocityComponent(STONE_SPEED, STONE_SPEED));
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(STONE));
    stone.add(dc);
    return stone;
  }
}
