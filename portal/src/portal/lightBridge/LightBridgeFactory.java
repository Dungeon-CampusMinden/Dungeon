package portal.lightBridge;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import portal.lightWall.BeamComponent;
import portal.lightWall.EmitterComponent;
import portal.portals.components.PortalIgnoreComponent;

/**
 * Factory for creating and managing light bridges and their emitters. A light bridge consists of
 * segments that are spawned on activation and removed on deactivation. Pits underneath segments are
 * temporarily closed. Multiple overlapping bridges can cover the same pit; a simple reference count
 * (GLOBAL_PIT_STATE) ensures a pit stays closed while at least one bridge covers it. When the last
 * covering bridge is removed, the pit's original state (open/closed) and original timeToOpen are
 * restored.
 */
public class LightBridgeFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH =
      new SimpleIPath("portal/light_bridge");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
      new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
      new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  /** Number of tiles by which the extended start point is offset in front of the emitter. */
  public static int spawnOffset = 1;

  private static final LevelElement[] stoppingTiles = {
    LevelElement.WALL, LevelElement.PORTAL, LevelElement.GLASSWALL
  };

  /**
   * Creates a new light bridge emitter at the given position and direction. Can be spawned active
   * or inactive.
   *
   * @param position Position of the emitter
   * @param direction Direction of the light bridge
   * @param active true if the emitter should be initially active
   * @return The created emitter entity
   */
  public static Entity createEmitter(Point position, Direction direction, boolean active) {
    Entity emitter = new Entity("wallEmitter");
    PositionComponent pc = new PositionComponent(position);
    pc.rotation(rotationFor(direction));
    pc.viewDirection(direction);
    emitter.add(new BeamComponent());
    emitter.add(new EmitterComponent());
    emitter.add(pc);
    emitter.add(new DrawComponent(active ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE));
    emitter.add(
        new CollideComponent(
            Vector2.of(0f, 0f),
            Vector2.of(1f, 1f),
            CollideComponent.DEFAULT_COLLIDER,
            CollideComponent.DEFAULT_COLLIDER));
    emitter.add(new PortalIgnoreComponent());
    //    if (active) activate();
    return emitter;
  }

  /**
   * Activates a light bridge emitter.
   *
   * @param emitterEntity The emitter entity
   */
  public static void activate(Entity emitterEntity) {
    LightBridgeUtil.activate(emitterEntity);
  }

  /**
   * Deactivates a light bridge emitter.
   *
   * @param emitterEntity The emitter entity
   */
  public static void deactivate(Entity emitterEntity) {
    LightBridgeUtil.deactivate(emitterEntity);
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
