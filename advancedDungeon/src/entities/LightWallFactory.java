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
 * Eine Factory-Klasse zum Erstellen von "Lichtwand"-Entitäten.
 *
 * <p>Diese Klasse stellt statische Methoden zur Verfügung, um Lichtwände zu erstellen, zu
 * aktivieren, zu deaktivieren und zu verwalten. Eine Lichtwand ist eine visuelle Barriere, die aus
 * einem Emitter und mehreren Segmenten besteht. Sie erstreckt sich von einem Startpunkt in eine
 * bestimmte Richtung, bis sie auf eine {@link WallTile} trifft. Die Segmente der Wand sind solide
 * und blockieren die Bewegung.
 */
public class LightWallFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_wall");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
    new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
    new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_inactive.png");

  /**
   * Komponente, die den Zustand und die Segmente einer einzelnen Lichtwand verwaltet.
   *
   * <p>Diese Komponente wird an die Emitter-Entität angehängt und steuert die Logik zum
   * Aktivieren und Deaktivieren der Wand, einschließlich des Erstellens und Entfernens der
   * einzelnen Wandsegmente.
   */
  public static class LightWallComponent implements Component {
    private final Entity owner;
    private final Direction direction;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    private final Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

    /**
     * Erstellt eine neue LightWallComponent.
     *
     * @param owner Die Entität, zu der diese Komponente gehört (der Emitter).
     * @param direction Die Richtung, in die sich die Wand erstreckt.
     */
    public LightWallComponent(Entity owner, Direction direction) {
      this.owner = owner;
      this.direction = direction;
    }

    /**
     * Erstellt eine neue Emitter-Entität für eine Lichtwand.
     *
     * <p>Der Emitter ist der visuelle und logische Ursprung der Wand.
     *
     * @param from Der Startpunkt des Emitters.
     * @param direction Die Richtung, in die der Emitter zeigt und die Wand emittiert.
     * @return Die erstellte Emitter-Entität.
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

    /**
     * Gibt eine schreibgeschützte Liste der Entitäten zurück, aus denen die Wandsegmente bestehen.
     *
     * @return Eine unveränderliche Liste der Segment-Entitäten.
     */
    public List<Entity> getSegments() {
      return Collections.unmodifiableList(segments);
    }

    /** Aktiviert die Lichtwand, wodurch alle ihre Segmente erstellt und dem Spiel hinzugefügt werden. */
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

    /** Deaktiviert die Lichtwand, entfernt alle ihre Segmente aus dem Spiel. */
    private void deactivate() {
      if (!active) return;
      this.active = false;

      segments.forEach(Game::remove);
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
      owner.name(on ? "lightWallEmitter" : "lightWallEmitterInactive");

      owner.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    }

    /**
     * Erstellt ein einzelnes Segment der Lichtwand an einer interpolierten Position.
     *
     * @param from Der Startpunkt der gesamten Wand.
     * @param to Der Endpunkt der gesamten Wand.
     * @param totalPoints Die Gesamtzahl der Segmente.
     * @param currentIndex Der Index des zu erstellenden Segments.
     * @return Die erstellte Segment-Entität.
     */
    private Entity createNextSegment(Point from, Point to, int totalPoints, int currentIndex) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Point currentPoint = new Point(x, y);
      PositionComponent pc = new PositionComponent(currentPoint);

      Entity segment = new Entity("lightWallSegment");
      segment.add(pc);

      segment.add(new CollideComponent()); // Fügt die Kollisionskomponente hinzu

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
   * Erstellt eine vollständige, steuerbare Lichtwand und gibt die Emitter-Entität zurück.
   *
   * @param from Der Startpunkt, an dem der Emitter platziert wird.
   * @param direction Die Richtung, in die sich die Wand erstrecken soll.
   * @param active Gibt an, ob die Wand initial aktiviert sein soll.
   * @return Die Emitter-Entität, die die Lichtwand steuert.
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

  /**
   * Aktiviert eine gegebene Lichtwand.
   *
   * @param wallEmitter Die Emitter-Entität der zu aktivierenden Wand.
   */
  public static void activateWall(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::activate);
  }

  /**
   * Deaktiviert eine gegebene Lichtwand.
   *
   * @param wallEmitter Die Emitter-Entität der zu deaktivierenden Wand.
   */
  public static void deactivateWall(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::deactivate);
  }

  /**
   * Gibt eine schreibgeschützte Liste der Segment-Entitäten für eine gegebene Wand zurück.
   *
   * @param wallEmitter Die Emitter-Entität der Wand.
   * @return Eine Liste der Segment-Entitäten oder eine leere Liste, wenn die Wand nicht gefunden
   *     wurde.
   */
  public static List<Entity> getWallSegments(Entity wallEmitter) {
    return wallEmitter
      .fetch(LightWallComponent.class)
      .map(LightWallComponent::getSegments)
      .orElse(Collections.emptyList());
  }

  /**
   * Berechnet die Anzahl der Segmente, die benötigt werden, um die Distanz zwischen zwei Punkten zu
   * überbrücken.
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
   * Berechnet den Endpunkt einer Wand, der sich vom Startpunkt in eine Richtung erstreckt, bis eine
   * {@link WallTile} erreicht wird.
   *
   * @param from Der Startpunkt.
   * @param beamDirection Die Richtung der Wand.
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
