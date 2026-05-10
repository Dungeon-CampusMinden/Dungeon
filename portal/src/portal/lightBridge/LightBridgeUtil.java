package portal.lightBridge;

import contrib.components.CollideComponent;
import contrib.hud.DialogUtils;
import contrib.utils.components.collide.Hitbox;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import portal.PortalRegistry;
import portal.lightWall.BeamComponent;
import portal.lightWall.BeamExtendedComponent;
import portal.lightWall.EmitterComponent;
import portal.lightWall.LightWallFactory;
import portal.portals.components.PortalExtendComponent;

/** Utility Class for everything LightBridge related. */
public class LightBridgeUtil {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH =
      new SimpleIPath("portal/light_bridge");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
      new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
      new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");
  private static final LevelElement[] stoppingTiles = {
    LevelElement.WALL, LevelElement.PORTAL, LevelElement.GLASSWALL
  };
  private static final ConcurrentHashMap<PitTile, Object[]> GLOBAL_PIT_STATE =
      new ConcurrentHashMap<>();
  private static final Set<PitTile> myCoveredPits = new java.util.HashSet<>();

  private static Map<String, Animation> SEGMENT_ANIMATION_CACHE;

  private static Map<String, Animation> segmentAnimations() {
    if (SEGMENT_ANIMATION_CACHE == null) {
      SEGMENT_ANIMATION_CACHE = Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
    }
    return SEGMENT_ANIMATION_CACHE;
  }

  /**
   * Updates the visual representation of the emitter.
   *
   * @param emitter the emitter which is to be updated
   * @param on true if active; false if inactive
   */
  private static void updateEmitterVisual(Entity emitter, boolean on) {
    DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);
    emitter.name(on ? "lightWallEmitter" : "lightWallEmitterInactive");
  }

  /**
   * Activates the beam and creates segments and collider.
   *
   * @param emitter the emitter that should be activated.
   */
  public static void activate(Entity emitter) {
    emitter
        .fetch(EmitterComponent.class)
        .ifPresent(
            emitterComponent -> {
              if (emitterComponent.isActive()) return; // mehrfaches Aktivieren verhindern
              emitterComponent.setActive(true);
              PositionComponent emitterPosition = emitter.fetch(PositionComponent.class).get();
              BeamComponent beamComponent = emitter.fetch(BeamComponent.class).get();
              Point end =
                  calculateEndPoint(emitterPosition.position(), emitterPosition.viewDirection());
              createSegments(
                  emitterPosition.position(),
                  end,
                  emitterPosition.viewDirection(),
                  beamComponent,
                  false);
              createCollider(
                  emitter, emitterPosition.viewDirection(), emitterPosition.position(), end, true);
              coverPit(beamComponent);
              updateEmitterVisual(emitter, true);
            });
  }

  /**
   * Deactivates the beam and removes segments and collider.
   *
   * @param emitter the emitter that should be deactivated.
   */
  public static void deactivate(Entity emitter) {
    emitter
        .fetch(EmitterComponent.class)
        .ifPresent(
            emitterComponent -> {
              if (!emitterComponent.isActive()) return; // / mehrfaches Deaktivieren verhindern
              emitterComponent.setActive(false);
              BeamComponent beamComponent = emitter.fetch(BeamComponent.class).get();
              getRelevantEntities(beamComponent)
                  .filter(entity -> !entity.isPresent(EmitterComponent.class))
                  .forEach(Game::remove);
              emitter.fetch(CollideComponent.class).ifPresent(cc -> cc.collider(new Hitbox(0, 0)));
              uncoverPit();
              updateEmitterVisual(emitter, false);
            });
  }

  /**
   * Creates the segments of the light bridge between two points.
   *
   * @param from Start point
   * @param to End point
   * @param direction Direction The count is based on the maximum delta in x or y. Each segment gets
   *     its position and rotation. Performance note: simple linear interpolation; for very long
   *     bridges consider streaming/tiling.
   * @param beamComponent BeamComponent from the Emitter.
   * @param isExtended if true, marks the segments as extended via the Portal.
   */
  private static void createSegments(
      Point from, Point to, Direction direction, BeamComponent beamComponent, boolean isExtended) {
    int totalPoints = (int) Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y())) + 1;
    float x;
    float y;
    for (int i = 0; i < totalPoints; i++) {
      x = from.x() + i * (to.x() - from.x()) / (totalPoints - 1);
      y = from.y() + i * (to.y() - from.y()) / (totalPoints - 1);
      Entity segment = new Entity("lightWallSegment");
      segment.add(beamComponent);
      segment.add(new PositionComponent(new Point(x, y)));
      segment.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
      AnimationConfig cfg = segmentAnimations().get("idle").getConfig();
      State idle = new State("idle", SEGMENT_SPRITESHEET_PATH, cfg);
      StateMachine sm = new StateMachine(List.of(idle));
      DrawComponent dc = new DrawComponent(sm);
      dc.depth(DepthLayer.Ground.depth());
      segment.add(dc);
      if (isExtended) {
        segment.add(new BeamExtendedComponent());
      }
      Game.add(segment);
    }
  }

  /**
   * Pit logic (cover): For each segment, check the tile underneath. If it is a PitTile and not yet
   * registered by this beam, add it to the local set and increment the global reference counter. On
   * the first cover for a pit, store the original open-state and previous timeToOpen, then close it
   * and set timeToOpen far into the future. Subsequent covers only increment the counter.
   *
   * @param beamComponent BeamComponent from the Emitter.
   */
  public static void coverPit(BeamComponent beamComponent) {
    getRelevantEntities(beamComponent)
        .forEach(
            segment -> {
              Point pos =
                  segment
                      .fetch(PositionComponent.class)
                      .map(PositionComponent::position)
                      .orElse(null);
              if (pos == null) return;
              Tile tile = Game.tileAt(pos).orElse(null);
              if (!(tile instanceof PitTile pit)) return;
              if (!myCoveredPits.add(pit)) return; // beam already tracked this pit
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
            });
  }

  /**
   * Pit logic (uncover): For each pit covered by this beam, decrement the global counter. When it
   * reaches zero, restore the original timeToOpen and open/close state, then remove the entry. The
   * local set is cleared afterward.
   */
  public static void uncoverPit() {
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
   * Creates a Collider for the Portals to collide with, with the given size position and direciton.
   *
   * @param emitter The emitter that gets the new Collider.
   * @param direction Direction of the Collider.
   * @param start Start Point.
   * @param end End Point.
   * @param hasPEC True if its the base Emitter.
   */
  private static void createCollider(
      Entity emitter, Direction direction, Point start, Point end, boolean hasPEC) {
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
    emitter.remove(CollideComponent.class);
    CollideComponent cc =
        new CollideComponent(Vector2.of(offsetX, offsetY), Vector2.of(width, height));
    cc.isSolid(false);
    emitter.add(cc);

    if (hasPEC) {
      PortalExtendComponent pec = new PortalExtendComponent();
      pec.onExtend =
          (dir, exitPosition, portalExtendComponent) -> {
            Point startPoint = exitPosition.translate(dir);
            Point endPoint = calculateEndPoint(startPoint, dir);
            BeamComponent beamComponent = emitter.fetch(BeamComponent.class).get();
            Entity extendedEmitter = LightWallFactory.createEmitter(startPoint, dir, true);
            createSegments(startPoint, endPoint, dir, beamComponent, true);
            extendedEmitter.remove(DrawComponent.class);
            extendedEmitter.remove(EmitterComponent.class);
            extendedEmitter.remove(BeamComponent.class);
            extendedEmitter.add(beamComponent);
            extendedEmitter.add(new BeamExtendedComponent());
            createCollider(extendedEmitter, dir, startPoint, endPoint, false);
            Game.add(extendedEmitter);
          };
      pec.onTrim =
          (e) -> {
            getRelevantEntities(e.fetch(BeamComponent.class).get())
                .filter(entity -> !entity.isPresent(EmitterComponent.class))
                .filter(entity -> entity.isPresent(BeamExtendedComponent.class))
                .forEach(Game::remove);
          };
      emitter.add(pec);
    }
  }

  /**
   * Calculates the end point by stepping from the start in the beam's direction until a WallTile,
   * PortalTile, or Glass Wand Tile is reached or no tile exists. Returns the last traversable
   * point.
   *
   * @param from Starting point
   * @param beamDirection Direction of the beam
   * @return Returns the calculated end point of the beam.
   */
  private static Point calculateEndPoint(Point from, Direction beamDirection) {
    try {
      return PortalRegistry.getCalculations()
          .calculateLightWallAndBridgeEnd(from, beamDirection, stoppingTiles);
    } catch (Exception e) {
      if (PortalRegistry.isDebugMode()) e.printStackTrace();
      DialogUtils.showTextPopup("Da stimmt etwas mit meinen Berechnungen nicht,", "Code Error");
    }
    return from;
  }

  /**
   * Returns a Stream all Entities with the same Component given as the parameter.
   *
   * @param beamComponent Component to filter for.
   * @return Stream of all Entities that have the given Component.
   */
  private static Stream<Entity> getRelevantEntities(BeamComponent beamComponent) {
    return Game.levelEntities(Set.of(BeamComponent.class))
        .filter(entity -> entity.fetch(BeamComponent.class).get().equals(beamComponent));
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
