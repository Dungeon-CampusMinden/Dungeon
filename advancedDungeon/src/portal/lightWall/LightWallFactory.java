package portal.lightWall;

import contrib.components.CollideComponent;
import contrib.utils.components.collide.Hitbox;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;

/**
 * Factory class for creating and managing light walls and their emitters. Provides methods to
 * create, activate, and deactivate light wall emitters.
 */
public class LightWallFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_wall");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
      new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
      new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_inactive.png");

  /** Number of tiles by which the extended start point is offset in front of the emitter. */
  public static int spawnOffset = 1;

  private static final LevelElement[] stoppingTiles = {
    LevelElement.WALL, LevelElement.PORTAL, LevelElement.GLASSWALL
  };

  /**
   * Creates a new light wall emitter at the given position and direction. Can be spawned active or
   * inactive.
   *
   * @param position Position of the emitter
   * @param direction Direction of the light wall
   * @param active true if the emitter should be initially active
   * @return The created emitter entity
   */
  public static Entity createEmitter(Point position, Direction direction, boolean active) {
    EmitterComponent emitterComponent = new EmitterComponent(position, direction, active);
    return emitterComponent.getEmitter();
  }

  /**
   * Activates a light wall emitter.
   *
   * @param emitterEntity The emitter entity
   */
  public static void activate(Entity emitterEntity) {
    emitterEntity.fetch(EmitterComponent.class).ifPresent(EmitterComponent::activate);
  }

  /**
   * Deactivates a light wall emitter.
   *
   * @param emitterEntity The emitter entity
   */
  public static void deactivate(Entity emitterEntity) {
    emitterEntity.fetch(EmitterComponent.class).ifPresent(EmitterComponent::deactivate);
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

  /* --------------------- Components --------------------- */

  /** Component representing a light wall emitter and managing its beams. */
  public static class EmitterComponent implements Component {

    private final Entity emitter;
    private final List<Component> beams = new ArrayList<>();

    /**
     * Creates a new emitter for light walls.
     *
     * @param start Start position of the emitter
     * @param direction The Direction in which the light wall is generated
     * @param active Whether the emitter is initially active
     */
    public EmitterComponent(Point start, Direction direction, boolean active) {
      this.emitter = new Entity("wallEmitter");
      emitter.add(this);
      PositionComponent pc = new PositionComponent(start);
      pc.rotation(rotationFor(direction));
      emitter.add(pc);
      updateEmitterVisual(false);
      emitter.add(
          new CollideComponent(
              Vector2.of(0f, 0f),
              Vector2.of(1f, 1f),
              CollideComponent.DEFAULT_COLLIDER,
              (a, b, c) -> {}));
      beams.add(new BeamComponent(emitter, start, direction, true));
      emitter.add(new PortalIgnoreComponent());
      if (active) activate();
    }

    /**
     * Returns the emitter entity.
     *
     * @return Emitter entity
     */
    public Entity getEmitter() {
      return emitter;
    }

    /**
     * Updates the visual representation of the emitter.
     *
     * @param on true if active; false if inactive
     */
    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      emitter.add(dc);
      emitter.name(on ? "lightWallEmitter" : "lightWallEmitterInactive");
    }

    /** Activates the emitter and all associated beams. */
    public void activate() {
      trim();
      beams.forEach(
          beam -> {
            if (beam instanceof BeamComponent b) {
              b.activate();
            }
          });
      updateEmitterVisual(true);
    }

    /** Deactivates the emitter and all associated beams. */
    public void deactivate() {
      beams.forEach(
          beam -> {
            if (beam instanceof BeamComponent b) {
              b.deactivate();
            }
          });
      updateEmitterVisual(false);
    }

    /**
     * Adds a new beam and activates it.
     *
     * @param beam Beam component to add
     */
    public void extend(BeamComponent beam) {
      beams.add(beam);
      beam.activate();
    }

    /** Removes non-extendable beams. */
    public void trim() {
      beams.removeIf(
          beam -> {
            if (beam instanceof BeamComponent b && !b.extendable) {
              b.deactivate();
              return true;
            }
            return false;
          });
    }
  }

  /** Component representing a light beam between the emitter and a wall. */
  public static class BeamComponent implements Component {

    private static Map<String, Animation> SEGMENT_ANIMATION_CACHE;

    private static Map<String, Animation> segmentAnimations() {
      if (SEGMENT_ANIMATION_CACHE == null) {
        SEGMENT_ANIMATION_CACHE = Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
      }
      return SEGMENT_ANIMATION_CACHE;
    }

    private final Entity emitter;
    private final Direction direction;
    private final Point start;
    private final Boolean extendable;
    private final List<Entity> segments = new ArrayList<>();
    private final Entity collider = new Entity("lightWallCollider");
    private boolean active = false;
    private int trimCounter = 0;

    /**
     * Creates a new BeamComponent.
     *
     * @param owner Emitter entity
     * @param start Start point of the beam
     * @param direction Direction of the beam
     * @param extendable true if extendable
     */
    public BeamComponent(Entity owner, Point start, Direction direction, Boolean extendable) {
      this.emitter = owner;
      this.direction = direction;
      this.start = start;
      this.extendable = extendable;
      Game.add(collider);
      if (extendable) {
        PortalExtendComponent pec = new PortalExtendComponent();
        pec.onExtend =
            (d, e, portalExtendComponent) -> {
              Point startPoint = e.translate(d.scale(spawnOffset));
              emitter
                  .fetch(EmitterComponent.class)
                  .ifPresent(ec -> ec.extend(new BeamComponent(emitter, startPoint, d, false)));
            };
        pec.onTrim =
            (e) -> {
              emitter.fetch(EmitterComponent.class).ifPresent(EmitterComponent::trim);
              trimCounter++;
              System.out.println("Trim called from pec" + " " + trimCounter);
            };
        collider.add(pec);
      }
    }

    /** Activates the beam and creates segments and collider. */
    public void activate() {
      if (active) return; // mehrfaches Aktivieren verhindern
      active = true;
      Point end = calculateEndPoint(start, direction);
      createSegments(start, end, direction);
      createCollider(start, end, direction);
    }

    /** Deactivates the beam and removes segments and collider. */
    public void deactivate() {
      if (!active) return; // mehrfaches Deaktivieren verhindern
      active = false;
      segments.forEach(Game::remove);
      collider.fetch(CollideComponent.class).ifPresent(cc -> cc.collider(new Hitbox(0, 0)));
    }

    /**
     * Creates the segments of the light wall between two points.
     *
     * @param from Start point
     * @param to End point
     * @param direction Direction
     */
    private void createSegments(Point from, Point to, Direction direction) {
      int totalPoints =
          (int) Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y())) + 1;
      float x;
      float y;
      for (int i = 0; i < totalPoints; i++) {
        x = from.x() + i * (to.x() - from.x()) / (totalPoints - 1);
        y = from.y() + i * (to.y() - from.y()) / (totalPoints - 1);
        Entity segment = new Entity("lightWallSegment");
        segment.add(new PositionComponent(new Point(x, y)));
        segment.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
        AnimationConfig cfg = segmentAnimations().get("idle").getConfig();
        State idle = new State("idle", SEGMENT_SPRITESHEET_PATH, cfg);
        StateMachine sm = new StateMachine(List.of(idle));
        DrawComponent dc = new DrawComponent(sm);
        dc.depth(DepthLayer.Ground.depth());
        segment.add(dc);
        segments.add(segment);
      }
      segments.forEach(Game::add);
    }

    /**
     * Creates the collider for the light wall.
     *
     * @param start Start point
     * @param end End point
     * @param direction Direction
     */
    private void createCollider(Point start, Point end, Direction direction) {
      float width = 1f, height = 1f, offsetX = 0f, offsetY = 0f;
      if (direction == Direction.LEFT || direction == Direction.RIGHT) {
        float len = Math.abs(end.x() - start.x()) + 1f;
        width = Math.max(1f, len);
        offsetX = (direction == Direction.LEFT) ? -(width - 1f) : 0f;
      } else if (direction == Direction.UP || direction == Direction.DOWN) {
        float len = Math.abs(end.y() - start.y()) + 1f;
        height = Math.max(1f, len);
        offsetY = (direction == Direction.DOWN) ? -(height - 1f) : 0f;
      }
      PositionComponent pc = new PositionComponent(start);
      pc.rotation(rotationFor(direction));
      collider.add(pc);
      collider.remove(CollideComponent.class);
      CollideComponent cc =
          new CollideComponent(
              Vector2.of(offsetX, offsetY),
              Vector2.of(width, height),
              CollideComponent.DEFAULT_COLLIDER,
              (a, b, c) -> {});
      collider.add(cc);
    }

    /**
     * Calculates the end point of the beam based on the direction and obstacles. Stops at walls,
     * portal walls, and glass walls.
     *
     * @param from Start point
     * @param beamDirection Direction
     * @return End point of the beam
     */
    private Point calculateEndPoint(Point from, Direction beamDirection) {
      Point lastPoint = from;
      Point currentPoint = from;

      while (true) {
        Tile currentTile = Game.tileAt(currentPoint).orElse(null);
        if (currentTile == null) break;

        if (Arrays.asList(stoppingTiles).contains(currentTile.levelElement())) break;

        lastPoint = currentPoint;
        currentPoint = currentPoint.translate(beamDirection);
      }
      return lastPoint;
    }
  }
}
