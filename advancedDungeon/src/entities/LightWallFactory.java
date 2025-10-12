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
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LightWallFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_wall");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE = new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE = new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_inactive.png");

  /**
   * Komponente, die den Zustand und die Segmente einer einzelnen Lichtbrücke verwaltet.
   */
  public static class LightWallComponent implements Component {
    private final Entity owner;
    private final Direction direction;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    private final Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

    public LightWallComponent(Entity owner, Direction direction) {
      this.owner = owner;
      this.direction = direction;
    }

    /**
     * Erstellt eine neue Emitter-Entität mit einer LightBridgeComponent.
     */
    public static Entity createEmitterForWall(Point from, Direction direction) {
      Entity emitter = new Entity("lightWallEmitter");
      PositionComponent pc = new PositionComponent(from);
      pc.rotation(rotationFor(direction));
      emitter.add(pc);

      emitter.add(new CollideComponent());
      DrawComponent dc = new DrawComponent(EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      emitter.add(dc);

      return emitter;
    }

    public List<Entity> getSegments() {
      return Collections.unmodifiableList(segments);
    }

    private void activate() {
      if (active) return;
      this.active = true;

      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        Point start = pc.position();
        Point end = calculateEndPoint(start, this.direction);
        int totalPoints = calculateNumberOfPoints(start, end);

        for (int i = 0; i < totalPoints; i++) {
          Entity segment = createNextSegment(start, end, totalPoints, i);
          this.segments.add(segment);
          Game.add(segment);
        }
      });
      updateEmitterVisual(true);
    }

    private void deactivate() {
      if (!active) return;
      this.active = false;

      segments.forEach(segment -> {
        Game.remove(segment);
      });
      segments.clear();
      updateEmitterVisual(false);
    }


    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      owner.add(dc);
      owner.name(on ? "lightWallEmitter" : "lightWallEmitterInactive");


      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        pc.rotation(rotationFor(direction));
      });
    }

    private Entity createNextSegment(Point from, Point to, int totalPoints, int currentIndex) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Point currentPoint = new Point(x, y);
      PositionComponent pc = new PositionComponent(currentPoint);

      Entity segment = new Entity("lightWallSegment");
      segment.add(pc);

      segment.add(new CollideComponent()); // Fügt die Kollisionskomponente hinzu

      pc.rotation(rotationFor(direction));

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
  }

  /**
   * Erstellt eine steuerbare Lichtbrücke und gibt die Emitter-Entität zurück.
   */
  public static Entity createLightWall(Point from, Direction direction, boolean active) {

    Entity emitter = LightWallComponent.createEmitterForWall(from, direction);

    LightWallComponent wallComponent = new LightWallComponent(emitter, direction);
    emitter.add(wallComponent);

    if (active) {
      emitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::activate);
    }

    Game.add(emitter);
    return emitter;
  }

  public static void extendWall(Point from, Direction direction, Entity owner) {
    LightWallComponent bridgeComponent = new LightWallComponent(owner, direction);
    owner.add(bridgeComponent);
  }

  /**
   * Aktiviert eine gegebene Lichtbrücke.
   */
  public static void activateWall(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::activate);
  }

  /**
   * Deaktiviert eine gegebene Lichtbrücke.
   */
  public static void deactivateWall(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::deactivate);
  }

  /**
   * Gibt eine schreibgeschützte Liste der Segment-Entitäten für eine gegebene Brücke zurück.
   */
  public static List<Entity> getWallSegments(Entity wallEmitter) {
    return wallEmitter.fetch(LightWallComponent.class)
      .map(LightWallComponent::getSegments)
      .orElse(Collections.emptyList());
  }

  // Statische Hilfsmethoden
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
