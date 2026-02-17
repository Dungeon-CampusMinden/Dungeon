package portal.laser;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.List;
import java.util.Map;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;

/** This factory can create the laser that interact with the LaserCube and LaserReceiver. */
public class LaserFactory {

  private static final SimpleIPath LASER = new SimpleIPath("portal/laser");
  private static final SimpleIPath EMITTER_ACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_active.png");
  private static final SimpleIPath EMITTER_INACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_inactive.png");

  /**
   * Creates the laser, either active or not active.
   *
   * @param from starting point of the laser.
   * @param direction direction the laser is facing.
   * @return the laser entity.
   */
  public static Entity createLaser(Point from, Direction direction) {
    Entity emitter = createEmitter(from, direction);
    LaserComponent laserComponent = new LaserComponent(false);
    emitter.add(laserComponent);
    emitter.add(new LaserEmitterComponent());
    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend =
        (outputDirection, point, portalExtendComponent) -> {
          LaserUtil.extendLaser(outputDirection, point, portalExtendComponent, laserComponent);
        };
    pec.onTrim = LaserUtil::trimLaser;
    emitter.add(pec);
    return emitter;
  }

  /**
   * Creates an emitter entity.
   * @param position position of the emitter.
   * @param direction direction the emitter is facing.
   * @return the emitter entity.
   */
  public static Entity createEmitter(Point position, Direction direction) {
    Entity emitter = new Entity("laserEmitter");
    PositionComponent pc = new PositionComponent(position);
    pc.rotation(rotationFor(direction));
    pc.viewDirection(direction);
    emitter.add(pc);
    emitter.add(new PortalIgnoreComponent());
    DrawComponent dc = new DrawComponent(EMITTER_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);
    return emitter;
  }

  /**
   * Creates a visual segment for the laser when extended.
   *
   * @param from position where the laser starts.
   * @param to position where the laser ends.
   * @param totalPoints how long the laser is.
   * @param index which segment this is.
   * @param dir in which direction the laser is extending.
   * @return the segment entity.
   */
  public static Entity createSegment(
      Point from, Point to, int totalPoints, int index, Direction dir) {
    int denom = Math.max(totalPoints - 1, 1);
    float x = from.x() + index * (to.x() - from.x()) / denom;
    float y = from.y() + index * (to.y() - from.y()) / denom;
    Point p = new Point(x, y);

    Entity segment = new Entity("laserSegment");
    PositionComponent pc = new PositionComponent(p);
    segment.add(pc);

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(LASER);
    State idle;
    if (dir == Direction.LEFT || dir == Direction.RIGHT) {
      idle = State.fromMap(animationMap, "horizontal");
    } else {
      idle = State.fromMap(animationMap, "vertical");
    }

    StateMachine sm = new StateMachine(List.of(idle));
    DrawComponent dc = new DrawComponent(sm);
    segment.add(dc);

    return segment;
  }

  /**
   * Helper method that transforms a direction to its given degree.
   *
   * @param d the direction that is getting converted.
   * @return the resulting degree.
   */
  private static float rotationFor(Direction d) {
    return switch (d) {
      case UP -> 0f;
      case DOWN -> 180f;
      case LEFT -> 90f;
      case RIGHT -> -90f;
      default -> 0f;
    };
  }
}
