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

/**
 * Eine Factory-Klasse zum Erstellen von "Lichtbrücken"-Entitäten.
 *
 * <p>Diese Klasse bietet statische Methoden zum Erstellen und Verwalten von Lichtbrücken. Eine
 * Lichtbrücke ist eine begehbare Fläche, die sich über Abgründe ({@link PitTile}) erstreckt. Sie
 * besteht aus einem Emitter und mehreren Segmenten. Die Brücke wird von einem Startpunkt in eine
 * Richtung emittiert, bis sie auf eine {@link WallTile} trifft.
 *
 * <p>Im Gegensatz zu {@link LightWallFactory} sind die Segmente der Brücke nicht solide, sondern
 * schließen vorübergehend {@code PitTile}s, um sie passierbar zu machen.
 */
public class LightBridgeFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH =
    new SimpleIPath("portal/light_bridge");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
    new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
    new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  /**
   * Komponente, die den Zustand und die Segmente einer einzelnen Lichtbrücke verwaltet.
   *
   * <p>Diese Komponente wird an die Emitter-Entität angehängt und steuert die Logik zum
   * Aktivieren und Deaktivieren der Brücke. Dies beinhaltet das Erstellen der Segmente und das
   * Management von {@link PitTile}s, die von der Brücke abgedeckt werden.
   */
  public static class LightBridgeComponent implements Component {
    private final Entity owner;
    private final Direction direction;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    private final Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

    /**
     * Erstellt eine neue LightBridgeComponent.
     *
     * @param owner Die Entität, zu der diese Komponente gehört (der Emitter).
     * @param direction Die Richtung, in die sich die Brücke erstreckt.
     */
    public LightBridgeComponent(Entity owner, Direction direction) {
      this.owner = owner;
      this.direction = direction;
    }

    /**
     * Erstellt eine neue Emitter-Entität für eine Lichtbrücke.
     *
     * @param from Der Startpunkt des Emitters.
     * @param direction Die Richtung, in die der Emitter zeigt und die Brücke emittiert.
     * @return Die erstellte Emitter-Entität.
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

    /**
     * Gibt eine schreibgeschützte Liste der Entitäten zurück, aus denen die Brückensegmente
     * bestehen.
     *
     * @return Eine unveränderliche Liste der Segment-Entitäten.
     */
    public List<Entity> getSegments() {
      return Collections.unmodifiableList(segments);
    }

    /** Aktiviert die Lichtbrücke, erstellt ihre Segmente und schließt darunterliegende Abgründe. */
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
              Game.add(segment);
            }
          });
      updateEmitterVisual(true);
    }

    /**
     * Deaktiviert die Lichtbrücke, entfernt ihre Segmente und stellt den ursprünglichen Zustand der
     * Abgründe wieder her.
     */
    private void deactivate() {
      if (!active) return;
      this.active = false;

      segments.forEach(
        segment -> {
          segment
            .fetch(PositionComponent.class)
            .ifPresent(
              pc ->
                Game.tileAt(pc.position())
                  .ifPresent(
                    tile -> {
                      if (tile instanceof PitTile pit) {
                        this.uncoverPit(pit);
                      }
                    }));
          Game.remove(segment);
        });
      segments.clear();
      updateEmitterVisual(false);
    }

    /**
     * Aktualisiert die visuelle Darstellung des Emitters, um seinen aktiven oder inaktiven Zustand
     * widerzuspiegeln.
     *
     * @param on {@code true}, um die aktive Textur zu verwenden, {@code false} für die inaktive.
     */
    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      owner.add(dc);
      owner.name(on ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");

      owner.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    }

    /**
     * Erstellt ein einzelnes Segment der Lichtbrücke und covert ggf. eine {@link PitTile}.
     *
     * @param from Der Startpunkt der gesamten Brücke.
     * @param to Der Endpunkt der gesamten Brücke.
     * @param totalPoints Die Gesamtzahl der Segmente.
     * @param currentIndex Der Index des zu erstellenden Segments.
     * @return Die erstellte Segment-Entität.
     */
    private Entity createNextSegment(Point from, Point to, int totalPoints, int currentIndex) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Point currentPoint = new Point(x, y);
      PositionComponent pc = new PositionComponent(currentPoint);

      Game.tileAt(currentPoint)
        .ifPresent(
          tile -> {
            if (tile instanceof PitTile pit) coverPit(pit);
          });

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

    /**
     * Schließt eine {@link PitTile} vorübergehend und speichert ihren ursprünglichen Zustand.
     *
     * @param pit Die zu schließende Grube.
     */
    private void coverPit(PitTile pit) {
      coveredPits.computeIfAbsent(
        pit,
        k -> {
          boolean wasOpen = pit.isOpen();
          long originalTime = pit.timeToOpen();
          if (wasOpen) {
            if (originalTime == 0) pit.timeToOpen(3_600_000L);
            pit.close();
          }
          return new Object[] {wasOpen, originalTime};
        });
    }

    /**
     * Öffnet eine zuvor geschlossene {@link PitTile} wieder und stellt ihren Zustand wieder her.
     *
     * @param pit Die zu öffnende Grube.
     */
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

    /**
     * Berechnet die Rotation für die Grafik basierend auf der Richtung.
     *
     * @param d Die Richtung.
     * @return Der Rotationswinkel in Grad.
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

  /**
   * Erstellt eine vollständige, steuerbare Lichtbrücke und gibt die Emitter-Entität zurück.
   *
   * @param from Der Startpunkt, an dem der Emitter platziert wird.
   * @param direction Die Richtung, in die sich die Brücke erstrecken soll.
   * @param active Gibt an, ob die Brücke initial aktiviert sein soll.
   * @return Die Emitter-Entität, die die Lichtbrücke steuert.
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

  /**
   * Aktiviert eine gegebene Lichtbrücke.
   *
   * @param bridgeEmitter Die Emitter-Entität der zu aktivierenden Brücke.
   */
  public static void activateBridge(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
  }

  /**
   * Deaktiviert eine gegebene Lichtbrücke.
   *
   * @param bridgeEmitter Die Emitter-Entität der zu deaktivierenden Brücke.
   */
  public static void deactivateBridge(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::deactivate);
  }

  /**
   * Gibt eine schreibgeschützte Liste der Segment-Entitäten für eine gegebene Brücke zurück.
   *
   * @param bridgeEmitter Die Emitter-Entität der Brücke.
   * @return Eine Liste der Segment-Entitäten oder eine leere Liste, wenn die Brücke nicht gefunden
   *     wurde.
   */
  public static List<Entity> getBridgeSegments(Entity bridgeEmitter) {
    return bridgeEmitter
      .fetch(LightBridgeComponent.class)
      .map(LightBridgeComponent::getSegments)
      .orElse(Collections.emptyList());
  }

  /**
   * Berechnet die Anzahl der Segmente, die benötigt werden, um die Distanz zu überbrücken.
   *
   * @param from Der Startpunkt.
   * @param to Der Endpunkt.
   * @return Die Anzahl der benötigten Segmente.
   */
  private static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  /**
   * Berechnet den Endpunkt einer Brücke, bis eine {@link WallTile} erreicht wird.
   *
   * @param from Der Startpunkt.
   * @param beamDirection Die Richtung der Brücke.
   * @return Der letzte Punkt vor der Wand.
   */
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
