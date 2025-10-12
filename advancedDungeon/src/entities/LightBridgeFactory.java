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

public class LightBridgeFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_bridge");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  /**
   * Komponente, die den Zustand und die Segmente einer einzelnen Lichtbrücke verwaltet.
   */
  public static class LightBridgeComponent implements Component {
    private final Entity owner;
    private final Direction direction;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    private final Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

    public LightBridgeComponent(Entity owner, Direction direction) {
      this.owner = owner;
      this.direction = direction;
    }

    /**
     * Erstellt eine neue Emitter-Entität mit einer LightBridgeComponent.
     */
    public static Entity createEmitterForBridge(Point from, Direction direction) {
      Entity emitter = new Entity("lightBridgeEmitter");
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
        segment.fetch(PositionComponent.class).ifPresent(pc ->
          Game.tileAt(pc.position()).ifPresent(tile -> {
            if (tile instanceof PitTile pit) {
              this.uncoverPit(pit);
            }
          })
        );
        Game.remove(segment);
      });
      segments.clear();
      updateEmitterVisual(false);
    }


    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      owner.add(dc);
      owner.name(on ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");


      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        pc.rotation(rotationFor(direction));
      });
    }

    private Entity createNextSegment(Point from, Point to, int totalPoints, int currentIndex) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Point currentPoint = new Point(x, y);
      PositionComponent pc = new PositionComponent(currentPoint);

      Game.tileAt(currentPoint).ifPresent(tile -> {
        if (tile instanceof PitTile pit) coverPit(pit);
      });

      Entity segment = new Entity("lightBridgeSegment");
      segment.add(pc);

      //segment.add(new CollideComponent().isSolid(false));

      pc.rotation(rotationFor(direction));

      Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
      State idle = State.fromMap(animationMap, "idle");
      StateMachine sm = new StateMachine(List.of(idle));

      DrawComponent dc = new DrawComponent(sm);
      dc.depth(DepthLayer.Ground.depth());

      segment.add(dc);
      return segment;
    }

    private void coverPit(PitTile pit) {
      coveredPits.computeIfAbsent(pit, k -> {
        boolean wasOpen = pit.isOpen();
        long originalTime = pit.timeToOpen();
        if (wasOpen) {
          if (originalTime == 0) pit.timeToOpen(3_600_000L);
          pit.close();
        }
        return new Object[]{wasOpen, originalTime};
      });
    }

    private void uncoverPit(PitTile pit) {
      Object[] originalState = coveredPits.remove(pit);
      if (originalState != null) {
        boolean wasOpen = (boolean) originalState[0];
        long originalTimeToOpen = (long) originalState[1];
        pit.timeToOpen(originalTimeToOpen);
        if (wasOpen) pit.open();
        else pit.close();
      }
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
  public static Entity createLightBridge(Point from, Direction direction, boolean active) {

    Entity emitter = LightBridgeComponent.createEmitterForBridge(from, direction);

    LightBridgeComponent bridgeComponent = new LightBridgeComponent(emitter, direction);
    emitter.add(bridgeComponent);

    if (active) {
      emitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
    }

    Game.add(emitter);
    return emitter;
  }

  public static void extendBridge(Point from, Direction direction, Entity owner) {
    LightBridgeComponent bridgeComponent = new LightBridgeComponent(owner, direction);
    owner.add(bridgeComponent);
  }

  /**
   * Aktiviert eine gegebene Lichtbrücke.
   */
  public static void activateBridge(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
  }

  /**
   * Deaktiviert eine gegebene Lichtbrücke.
   */
  public static void deactivateBridge(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::deactivate);
  }

  /**
   * Gibt eine schreibgeschützte Liste der Segment-Entitäten für eine gegebene Brücke zurück.
   */
  public static List<Entity> getBridgeSegments(Entity bridgeEmitter) {
    return bridgeEmitter.fetch(LightBridgeComponent.class)
      .map(LightBridgeComponent::getSegments)
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
