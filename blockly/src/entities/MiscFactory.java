package entities;

import components.BlockComponent;
import components.PushableComponent;
import contrib.components.CollideComponent;
import contrib.components.LeverComponent;
import contrib.utils.ICommand;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Map;

/** Factory class for creating miscellaneous game entities. */
public class MiscFactory {

  private static final IPath STONE = new SimpleIPath("objects/stone/stone.png");
  private static final IPath PRESSURE_PLATE_ON =
      new SimpleIPath("objects/pressureplate/on/pressureplate_0.png");
  private static final IPath PRESSURE_PLATE_OFF =
      new SimpleIPath("objects/pressureplate/off/pressureplate_0.png");
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
    stone.add(new CollideComponent());
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(STONE));
    stone.add(dc);
    return stone;
  }

  /**
   * Creates a pressure plate entity at the given position.
   *
   * <p>The pressure plate is an entity that reacts to collisions by toggling its lever state.
   *
   * @param position The initial position of the pressure plate.
   * @return A new pressure plate entity lever and collision behavior.
   */
  public static Entity pressurePlate(Point position) {
    Entity pressurePlate = new Entity("pressureplate");
    pressurePlate.add(new PositionComponent(position.toCoordinate().toCenteredPoint()));
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(PRESSURE_PLATE_OFF));
    Map<String, Animation> animationMap =
        Map.of("off", dc.currentAnimation(), "on", Animation.fromSingleImage(PRESSURE_PLATE_ON));
    dc.animationMap(animationMap);
    dc.currentAnimation("off");
    pressurePlate.add(dc);
    LeverComponent lc = new LeverComponent(false, ICommand.EMPTY_COMMAND);
    pressurePlate.add(lc);
    TriConsumer<Entity, Entity, Tile.Direction> collide =
        (entity, entity2, direction) -> {
          lc.toggle();
          if (lc.isOn()) dc.currentAnimation("on");
          else dc.currentAnimation("off");
        };
    pressurePlate.add(new CollideComponent(collide, collide));
    return pressurePlate;
  }
}

