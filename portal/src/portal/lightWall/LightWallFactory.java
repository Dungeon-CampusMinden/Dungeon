package portal.lightWall;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import portal.portals.components.PortalIgnoreComponent;

/**
 * Factory class for creating and managing light walls and their emitters. Provides methods to
 * create, activate, and deactivate light wall emitters.
 */
public class LightWallFactory {

  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
    new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
    new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_inactive.png");

  /**
   * Creates a new light wall emitter at the given position and direction. Can be spawned active or
   * inactive.
   *
   * @param position  Position of the emitter
   * @param direction Direction of the light wall
   * @param active    true if the emitter should be initially active
   * @return The created emitter entity
   */
  public static Entity createEmitter(Point position, Direction direction, boolean active) {
    Entity emitter = new Entity("wallEmitter");
    emitter.add(new EmitterComponent());
    emitter.add(new BeamComponent());
    emitter.add(new PortalIgnoreComponent());
    PositionComponent pc = new PositionComponent(position);
    pc.rotation(rotationFor(direction));
    pc.viewDirection(direction);
    emitter.add(pc);
    DrawComponent dc = new DrawComponent(active ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
    emitter.add(dc);
    emitter.add(
      new CollideComponent(
        Vector2.of(0f, 0f),
        Vector2.of(1f, 1f),
        CollideComponent.DEFAULT_COLLIDER,
        CollideComponent.DEFAULT_COLLIDER));
//    if (active) LightWallUtil.activate(emitter);
    return emitter;
  }

  /**
   * Activates a light wall emitter.
   *
   * @param emitterEntity The emitter entity
   */
  public static void activate(Entity emitterEntity) {
    LightWallUtil.activate(emitterEntity);
  }

  /**
   * Deactivates a light wall emitter.
   *
   * @param emitterEntity The emitter entity
   */
  public static void deactivate(Entity emitterEntity) {
    LightWallUtil.deactivate(emitterEntity);
  }

  /**
   * Returns the rotation angle for the given direction.
   *
   * @param d Direction
   * @return Rotation angle in degrees
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
