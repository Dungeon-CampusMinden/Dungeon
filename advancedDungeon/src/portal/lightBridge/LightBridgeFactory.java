package portal.lightBridge;

import contrib.components.CollideComponent;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import contrib.utils.components.collide.Hitbox;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import portal.portals.abstraction.Calculations;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;
import starter.PortalStarter;

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

  private static final SimpleIPath PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyCalculations.java");
  private static final String CLASSNAME = "portal.riddles.MyCalculations";

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
    EmitterComponent emitterComponent = new EmitterComponent(position, direction, active);
    return emitterComponent.getEmitter();
  }

  /**
   * Activates a light bridge emitter.
   *
   * @param emitterEntity The emitter entity
   */
  public static void activate(Entity emitterEntity) {
    emitterEntity.fetch(EmitterComponent.class).ifPresent(EmitterComponent::activate);
  }

  /**
   * Deactivates a light bridge emitter.
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

  /** Component representing a light bridge emitter and managing its beams. */
  private static class EmitterComponent implements Component {

    private final Entity emitter;
    private final List<Component> beams = new ArrayList<>();

    /**
     * Creates a new emitter for light bridges.
     *
     * @param start Start position of the emitter
     * @param direction The Direction in which the light bridge is generated
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

    /**
     * Activates the emitter and all associated beams. Idempotent: repeated calls will not create
     * duplicate segments. Existing non-extendable beams are trimmed first to remove stale
     * structures.
     */
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

    /**
     * Deactivates the emitter and all associated beams. Idempotent: repeated calls have no
     * additional effect. Visuals are switched to inactive; beams perform their own segment and pit
     * cleanup.
     */
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

  /**
   * Component representing a light bridge between the emitter and the first blocking tile (e.g., a
   * wall).
   */
  private static class BeamComponent implements Component {

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

    private static final ConcurrentHashMap<PitTile, Object[]> GLOBAL_PIT_STATE =
        new ConcurrentHashMap<>();
    private final Set<PitTile> myCoveredPits = new java.util.HashSet<>();

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
        pec.onTrim = e -> emitter.fetch(EmitterComponent.class).ifPresent(EmitterComponent::trim);
        collider.add(pec);
      }
    }

    /**
     * Activates the beam, creates bridge segments and collider, and covers pits. Activates this
     * beam: 1. Idempotence guard prevents double activation. 2. The end point is determined by
     * marching in the beam direction until a wall or map boundary is reached. 3. Bridge segments
     * are created between start and end (with rotation matching the direction). 4. A non-solid
     * collider spanning the full length is added for collisions/interactions. 5. coverPit() applies
     * pit coverage with reference counting.
     */
    public void activate() {
      if (active) return;
      active = true;
      Point end = calculateEndPoint(start, direction, stoppingTiles);
      createSegments(start, end, direction);
      createCollider(start, end, direction);
      coverPit();
    }

    /**
     * Deactivates the beam, removes bridge segments and collider, and reopens pits. Deactivates
     * this beam: 1. Idempotence guard prevents double deactivation. 2. uncoverPit() decrements the
     * reference count; the last beam restores the pit's original state. 3. All segments are removed
     * and the list is cleared to avoid leaks. 4. The Collider is reset to an empty hitbox so no
     * collision remains.
     */
    public void deactivate() {
      if (!active) return;
      active = false;
      uncoverPit();
      segments.forEach(Game::remove);
      collider.fetch(CollideComponent.class).ifPresent(cc -> cc.collider(new Hitbox(0, 0)));
      segments.clear();
    }

    /**
     * Creates the segments of the light bridge between two points.
     *
     * @param from Start point
     * @param to End point
     * @param direction Direction The count is based on the maximum delta in x or y. Each segment
     *     gets its position and rotation. Performance note: simple linear interpolation; for very
     *     long bridges consider streaming/tiling.
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
     * Pit logic (cover): For each segment, check the tile underneath. If it is a PitTile and not
     * yet registered by this beam, add it to the local set and increment the global reference
     * counter. On the first cover for a pit, store the original open-state and previous timeToOpen,
     * then close it and set timeToOpen far into the future. Subsequent covers only increment the
     * counter.
     */
    public void coverPit() {
      for (Entity segment : segments) {
        Point pos =
            segment.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
        if (pos == null) continue;
        Tile tile = Game.tileAt(pos).orElse(null);
        if (!(tile instanceof PitTile pit)) continue;
        if (!myCoveredPits.add(pit)) continue; // beam already tracked this pit
        Object[] state = GLOBAL_PIT_STATE.get(pit);
        if (state == null) {
          boolean wasOpen = pit.isOpen();
          long prevT = pit.timeToOpen();
          pit.timeToOpen(60 * 60 * 1000L);
          pit.close();
          GLOBAL_PIT_STATE.put(pit, new Object[] {1, wasOpen, prevT});
        } else {
          int count = (Integer) state[0];
          state[0] = count + 1;
        }
      }
    }

    /**
     * Pit logic (uncover): For each pit covered by this beam, decrement the global counter. When it
     * reaches zero, restore the original timeToOpen and open/close state, then remove the entry.
     * The local set is cleared afterward.
     */
    public void uncoverPit() {
      for (PitTile pit : myCoveredPits) {
        Object[] state = GLOBAL_PIT_STATE.get(pit);
        if (state == null) continue; // shouldn't happen
        int count = (Integer) state[0];
        if (count > 1) {
          state[0] = count - 1;
          continue;
        }
        boolean wasOpen = (Boolean) state[1];
        long prevT = (Long) state[2];
        pit.timeToOpen(prevT);
        if (wasOpen) pit.open();
        else pit.close();
        GLOBAL_PIT_STATE.remove(pit);
      }
      myCoveredPits.clear();
    }

    /**
     * Creates the non-solid collider over the bridge. Width/height depend on the span along the
     * direction.
     *
     * @param start Start point
     * @param end End point
     * @param direction The Direction in which the collider extends
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
                  (a, b, c) -> {})
              .isSolid(false);
      collider.add(cc);
    }

    /**
     * Calculates the end point by stepping from the start in the beam's direction until a WallTile,
     * PortalTile, or Glass Wand Tile is reached or no tile exists. Returns the last traversable
     * point.
     *
     * @param from Starting point
     * @param beamDirection Direction of the beam
     * @param stoppingTiles List of tiles that should block the lightwall.
     * @return Returns the calculated end point of the beam.
     */
    private Point calculateEndPoint(
        Point from, Direction beamDirection, LevelElement[] stoppingTiles) {
      Object o = null;
      try {
        o = DynamicCompiler.loadUserInstance(PATH, CLASSNAME);
        Point endPoint =
            ((Calculations) (o)).calculateLightWallAndBridgeEnd(from, beamDirection, stoppingTiles);
        return endPoint;
      } catch (Exception e) {
        if (PortalStarter.DEBUG_MODE) e.printStackTrace();
        DialogUtils.showTextPopup("Da stimmt etwas mit meinen Berechnungen nicht,", "Code Error");
      }
      return from;
    }
  }
}
