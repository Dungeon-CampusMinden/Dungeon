package entities;

import contrib.components.CollideComponent;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.elements.tile.WallTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LightBridgeFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH =
    new SimpleIPath("portal/light_bridge");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
    new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
    new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  public static int spawnOffset = 1;

  public static class LightBridgeComponent implements Component {
    private final Entity owner;
    private final Direction direction;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    private final List<Entity> extendedSegments = new ArrayList<>();
    private final java.util.Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

    private Point baseEnd = null;
    private Point extendEnd = null;

    public LightBridgeComponent(Entity owner, Direction direction) {
      this.owner = owner;
      this.direction = direction;
    }

    public static Entity createEmitterForWall(Point from, Direction direction) {
      Entity emitter = new Entity("lightBridgeEmitter");
      PositionComponent pc = new PositionComponent(from);
      pc.rotation(rotationFor(direction));
      emitter.add(pc);

      DrawComponent dc = new DrawComponent(EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      emitter.add(dc);

      return emitter;
    }

    private void activate() {
      if (active) return;
      this.active = true;

      owner
        .fetch(PositionComponent.class)
        .ifPresent(
          pc -> {
            Point start = pc.position();
            Point end = calculateEndPoint(start, this.direction);
            int totalPoints = calculateNumberOfPoints(start, end);

            for (int i = 0; i < totalPoints; i++) {
              Entity segment = createNextSegment(start, end, totalPoints, i);
              this.segments.add(segment);
            }

            baseEnd = end;
            Point finalEnd = pickFurtherEnd(start);
            createColliderForBeam(start, finalEnd, this.direction);
          });

      // exakt gleicher Ablauf wie LightWall: erst extend übernehmen, dann alle hinzufügen
      segments.addAll(extendedSegments);
      segments.forEach(segment -> {
        Game.add(segment);
        segment.fetch(PositionComponent.class).ifPresent(spc ->
          Game.tileAt(spc.position()).ifPresent(tile -> { if (tile instanceof PitTile pit) coverPit(pit); })
        );
      });

      updateEmitterVisual(true);
    }

    private void deactivate() {
      if (!active) return;
      this.active = false;

      segments.forEach(
        segment -> {
          segment
            .fetch(PositionComponent.class)
            .ifPresent(
              pc -> Game.tileAt(pc.position()).ifPresent(tile -> { if (tile instanceof PitTile pit) this.uncoverPit(pit); })
            );
          Game.remove(segment);
        });
      segments.clear();
      updateEmitterVisual(false);

      owner.remove(CollideComponent.class);
    }

    public void extend(Direction direction, Point from) {
      trim();

      Point end = calculateEndPoint(from, direction);
      int totalPoints = calculateNumberOfPoints(from, end);
      for (int i = 0; i < totalPoints; i++) {
        Entity segment = createNextSegment(from, end, totalPoints, i);
        this.extendedSegments.add(segment);
      }
      extendEnd = end;

      if (active) {
        extendedSegments.forEach(segment -> {
          Game.add(segment);
          segment.fetch(PositionComponent.class).ifPresent(spc ->
            Game.tileAt(spc.position()).ifPresent(tile -> { if (tile instanceof PitTile pit) coverPit(pit); })
          );
        });
        segments.addAll(extendedSegments);

        owner.fetch(PositionComponent.class).ifPresent(pc -> {
          Point start = pc.position();
          Point finalEnd = pickFurtherEnd(start);
          createColliderForBeam(start, finalEnd, this.direction);
        });
      }
    }

    public void trim() {
      if (extendedSegments.isEmpty()) return;

      if (active) {
        extendedSegments.forEach(segment -> {
          segment.fetch(PositionComponent.class).ifPresent(spc ->
            Game.tileAt(spc.position()).ifPresent(tile -> { if (tile instanceof PitTile pit) uncoverPit(pit); })
          );
          Game.remove(segment);
        });
        segments.removeAll(extendedSegments);

        owner.fetch(PositionComponent.class).ifPresent(pc -> {
          if (baseEnd != null) createColliderForBeam(pc.position(), baseEnd, this.direction);
        });
      }
      extendedSegments.clear();
      extendEnd = null;
    }

    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      owner.add(dc);
      owner.name(on ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");

      owner.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    }

    private Entity createNextSegment(Point from, Point to, int totalPoints, int currentIndex) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Point currentPoint = new Point(x, y);
      PositionComponent pc = new PositionComponent(currentPoint);

      Entity segment = new Entity("lightBridgeSegment");
      segment.add(pc);

      pc.rotation(rotationFor(direction));

      Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
      State idle = State.fromMap(animationMap, "idle");
      StateMachine sm = new StateMachine(List.of(idle));

      DrawComponent dc = new DrawComponent(sm);
      dc.depth(DepthLayer.Ground.depth());

      segment.add(dc);
      return segment;
    }

    private static float rotationFor(Direction d) {
      return switch (d) {
        case UP -> 0f;
        case DOWN -> 180f;
        case LEFT -> 90f;
        case RIGHT -> -90f;
        default -> 0f;
      };
    }

    private void createColliderForBeam(Point start, Point end, Direction direction) {
      float width, height;
      float offsetX, offsetY;
      switch (direction) {
        case UP -> {
          float len = (float) (end.y() - start.y() + 1f);
          width = 1f; height = Math.max(1f, len);
          offsetX = 0f; offsetY = 0f;
        }
        case DOWN -> {
          float len = (float) (start.y() - end.y() + 1f);
          width = 1f; height = Math.max(1f, len);
          offsetX = 0f; offsetY = -height + 1f;
        }
        case RIGHT -> {
          float len = (float) (end.x() - start.x() + 1f);
          width = Math.max(1f, len); height = 1f;
          offsetX = 0f; offsetY = 0f;
        }
        case LEFT -> {
          float len = (float) (start.x() - end.x() + 1f);
          width = Math.max(1f, len); height = 1f;
          offsetX = -width + 1f; offsetY = 0f;
        }
        default -> { width = 1f; height = 1f; offsetX = 0f; offsetY = 0f; }
      }

      CollideComponent cc = new CollideComponent(
        Vector2.of(offsetX, offsetY),
        Vector2.of(width, height),
        CollideComponent.DEFAULT_COLLIDER,
        CollideComponent.DEFAULT_COLLIDER
      );
      cc.isSolid(false);
      owner.remove(CollideComponent.class);
      owner.add(cc);
    }

    private void coverPit(PitTile pit) {
      Object[] state = coveredPits.get(pit);
      if (state == null) {
        boolean wasOpen = pit.isOpen();
        long prevT = pit.timeToOpen();
        pit.timeToOpen(60 * 60 * 1000L);
        pit.close();
        coveredPits.put(pit, new Object[] {1, wasOpen, prevT});
      } else {
        int count = (Integer) state[0];
        state[0] = count + 1;
      }
    }

    private void uncoverPit(PitTile pit) {
      Object[] state = coveredPits.get(pit);
      if (state == null) return;
      int count = (Integer) state[0];
      if (count > 1) { state[0] = count - 1; return; }
      boolean wasOpen = (Boolean) state[1];
      long prevT = (Long) state[2];
      pit.timeToOpen(prevT);
      if (wasOpen) pit.open(); else pit.close();
      coveredPits.remove(pit);
    }

    private Point pickFurtherEnd(Point start) {
      Point candidateBase = (baseEnd != null) ? baseEnd : start;
      Point candidateExt = (extendEnd != null) ? extendEnd : candidateBase;
      return switch (direction) {
        case RIGHT -> (candidateExt.x() > candidateBase.x()) ? candidateExt : candidateBase;
        case LEFT -> (candidateExt.x() < candidateBase.x()) ? candidateExt : candidateBase;
        case UP -> (candidateExt.y() > candidateBase.y()) ? candidateExt : candidateBase;
        case DOWN -> (candidateExt.y() < candidateBase.y()) ? candidateExt : candidateBase;
        default -> candidateBase;
      };
    }
  }

  public static Entity createLightBridge(Point from, Direction direction, boolean active) {

    Entity emitter = LightBridgeComponent.createEmitterForWall(from, direction);

    LightBridgeComponent bridgeComponent = new LightBridgeComponent(emitter, direction);
    emitter.add(bridgeComponent);

    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend = (d, e, portalExtendComponent) -> {
      Point startPoint = e;
      Tile tileAtExit = Game.tileAt(startPoint).orElse(null);
      if (tileAtExit instanceof WallTile) {
        startPoint = startPoint.translate(d.scale(spawnOffset));
      }
      extendWall(emitter, startPoint, d);
    };
    pec.onTrim = (emitterEntity) -> trimWall(emitter);
    emitter.add(pec);

    if (active) {
      emitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
    }

    Game.add(emitter);
    return emitter;
  }

  public static void activate(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
  }

  public static void deactivate(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::deactivate);
  }

  private static void extendWall(Entity bridgeEmitter, Point from, Direction direction) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(c -> c.extend(direction, from));
  }

  private static void trimWall(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::trim);
  }

  private static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  private static Point calculateEndPoint(Point from, Direction beamDirection) {
    Point lastPoint = from;
    Point currentPoint = from;
    Tile currentTile = Game.tileAt(from).orElse(null);
    while (currentTile != null && !(currentTile instanceof WallTile)) {
      lastPoint = currentPoint;
      currentPoint = currentPoint.translate(beamDirection);
      currentTile = Game.tileAt(currentPoint).orElse(null);
    }
    return lastPoint;
  }
}
