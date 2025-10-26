package entities;

import contrib.components.CollideComponent;
import core.utils.Vector2;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.WallTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Eine Factory-Klasse zum Erstellen von "Lichtwand"-Entitäten.
 */
public class LightWallFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_wall");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
      new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
      new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_inactive.png");

  // Konfigurierbarer Spawn-Offset für Teleport-/Extend-Startpunkte vor dem Portal
  public static int spawnOffset = 1;

  /**
   * Komponente, die den Zustand und die Segmente einer einzelnen Lichtwand verwaltet.
   */
  public static class LightWallComponent implements Component {
    private final Entity owner;
    private final Direction direction;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    private final List<Entity> extendedSegments = new ArrayList<>();

    // Merker für Collider-Längen: Basis und Extend
    private Point baseEnd = null;
    private Point extendEnd = null;

    public LightWallComponent(Entity owner, Direction direction) {
      this.owner = owner;
      this.direction = direction;
    }

    public static Entity createEmitterForWall(Point from, Direction direction) {
      Entity emitter = new Entity("lightWallEmitter");
      PositionComponent pc = new PositionComponent(from);
      pc.rotation(rotationFor(direction));
      emitter.add(pc);

      DrawComponent dc = new DrawComponent(EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      emitter.add(dc);

      return emitter;
    }

    public void extend(Direction direction, Point from) {
      trim();

      Point end = calculateEndPoint(from, direction);
      int total = calculateNumberOfPoints(from, end);
      for (int i = 0; i < total; i++) {
        Entity segment = createNextSegment(from, end, total, i, direction);
        extendedSegments.add(segment);
      }
      extendEnd = end;

      if (active) {
        extendedSegments.forEach(Game::add);
        segments.addAll(extendedSegments);
        owner.fetch(PositionComponent.class)
            .ifPresent(pc -> createColliderForBeam(pc.position(), pickFurtherEnd(pc.position()), this.direction));
      }
    }

    public void trim() {
      if (extendedSegments.isEmpty()) return;
      if (active) {
        extendedSegments.forEach(Game::remove);
        segments.removeAll(extendedSegments);
        owner.fetch(PositionComponent.class)
            .ifPresent(pc -> {
              if (baseEnd != null) createColliderForBeam(pc.position(), baseEnd, this.direction);
            });
      }
      extendedSegments.clear();
      extendEnd = null;
    }

    private void activate() {
      if (active) return;
      active = true;

      owner
          .fetch(PositionComponent.class)
          .ifPresent(
              pc -> {
                Point start = pc.position();
                Point end = calculateEndPoint(start, this.direction);
                int total = calculateNumberOfPoints(start, end);
                for (int i = 0; i < total; i++) segments.add(createNextSegment(start, end, total, i, this.direction));
                baseEnd = end;
                createColliderForBeam(start, pickFurtherEnd(start), this.direction);
              });

      // Base + ggf. bereits definierte Extend-Segmente ins Spiel bringen
      segments.addAll(extendedSegments);
      segments.forEach(Game::add);
      updateEmitterVisual(true);
    }

    private void deactivate() {
      if (!active) return;
      active = false;
      segments.forEach(Game::remove);
      segments.clear();
      updateEmitterVisual(false);
      owner.remove(CollideComponent.class);
    }

    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      owner.add(dc);
      owner.name(on ? "lightWallEmitter" : "lightWallEmitterInactive");
      owner.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    }

    private Entity createNextSegment(Point from, Point to, int totalPoints, int currentIndex, Direction rotDir) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Entity segment = new Entity("lightWallSegment");
      segment.add(new PositionComponent(new Point(x, y)));
      segment.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(rotDir)));

      Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
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

    private void createColliderForBeam(Point start, Point end, Direction dir) {
      float width = 1f, height = 1f, offsetX = 0f, offsetY = 0f;
      if (dir == Direction.LEFT || dir == Direction.RIGHT) {
        float len = Math.abs(end.x() - start.x()) + 1f;
        width = Math.max(1f, len);
        // height bleibt 1f
        offsetX = (dir == Direction.LEFT) ? -(width - 1f) : 0f;
      } else if (dir == Direction.UP || dir == Direction.DOWN) {
        float len = Math.abs(end.y() - start.y()) + 1f;
        // width bleibt 1f
        height = Math.max(1f, len);
        offsetY = (dir == Direction.DOWN) ? -(height - 1f) : 0f;
      }

      CollideComponent cc =
          new CollideComponent(
              Vector2.of(offsetX, offsetY),
              Vector2.of(width, height),
              CollideComponent.DEFAULT_COLLIDER,
              (a, b, c) -> {});
      owner.remove(CollideComponent.class);
      owner.add(cc);
    }

    private Point pickFurtherEnd(Point start) {
      Point b = (baseEnd != null) ? baseEnd : start;
      Point e = (extendEnd != null) ? extendEnd : b;
      return switch (direction) {
        case RIGHT -> (e.x() > b.x()) ? e : b;
        case LEFT -> (e.x() < b.x()) ? e : b;
        case UP -> (e.y() > b.y()) ? e : b;
        case DOWN -> (e.y() < b.y()) ? e : b;
        default -> b;
      };
    }
  }

  public static Entity createLightWall(Point from, Direction direction, boolean active) {
    Entity emitter = LightWallComponent.createEmitterForWall(from, direction);
    LightWallComponent wallComponent = new LightWallComponent(emitter, direction);
    emitter.add(wallComponent);

    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend =
        (d, e, portalExtendComponent) -> {
          Point startPoint = e.translate(d.scale(spawnOffset));
          extendWall(emitter, startPoint, d);
        };
    pec.onTrim = (emitterEntity) -> trimWall(emitter);
    emitter.add(pec);

    if (active) emitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::activate);
    Game.add(emitter);
    return emitter;
  }

  public static void activate(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::activate);
  }

  public static void deactivate(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::deactivate);
  }

  private static void extendWall(Entity wallEmitter, Point from, Direction direction) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(c -> c.extend(direction, from));
  }

  private static void trimWall(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::trim);
  }

  private static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  private static Point calculateEndPoint(Point from, Direction beamDirection) {
    Point lastPoint = from;
    Point currentPoint = from;
    while (true) {
      Tile currentTile = Game.tileAt(currentPoint).orElse(null);
      if (currentTile == null) break;
      boolean isWall = currentTile instanceof WallTile;
      if (isWall) break;
      lastPoint = currentPoint;
      currentPoint = currentPoint.translate(beamDirection);
    }
    return lastPoint;
  }
}
