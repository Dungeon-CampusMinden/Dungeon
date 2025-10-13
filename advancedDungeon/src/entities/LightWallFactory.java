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
import java.util.LinkedHashMap;

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
    private final List<Entity> extendedSegments = new ArrayList<>();
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

    /**
     * Erstellt einen einzelnen Erweiterungsstrahl. Ein bereits vorhandener wird zuvor entfernt.
     *
     * @param from Startpunkt der Erweiterung.
     * @param direction Richtung der Erweiterung.
     */
    public void extend(Point from, Direction direction) {
      trim(); // Bestehende Erweiterung entfernen
      System.out.println(
          "DEBUG: extend() called. Defining new extended beam from "
              + from
              + " in direction "
              + direction);

      Point end = calculateEndPoint(from, direction);
      int totalPoints = calculateNumberOfPoints(from, end);
      for (int i = 0; i < totalPoints; i++) {
        Entity segment = createNextSegment(from, end, totalPoints, i, direction);
        this.extendedSegments.add(segment);
      }
      System.out.println(
          "DEBUG: Defined " + extendedSegments.size() + " segments for the extended beam.");

      if (active) {
        System.out.println("DEBUG: Wall is active, adding extended segments to game now.");
        extendedSegments.forEach(Game::add);
        segments.addAll(extendedSegments);
      }
    }

    /** Entfernt den Erweiterungsstrahl. */
    public void trim() {
      if (extendedSegments.isEmpty()) return;
      System.out.println("DEBUG: trim() called. Removing extended beam.");
      if (active) {
        System.out.println("DEBUG: Wall is active, removing extended segments from game.");
        extendedSegments.forEach(Game::remove);
        segments.removeAll(extendedSegments);
      }
      extendedSegments.clear();
      System.out.println("DEBUG: Extended beam definition cleared.");
    }

    /** Aktiviert die Lichtwand, wodurch alle ihre Segmente erstellt und dem Spiel hinzugefügt werden. */
    private void activate() {
      if (active) return;
      this.active = true;
      System.out.println("DEBUG: activate() called. Building wall.");

      // Basisstrahl aufbauen
      owner
        .fetch(PositionComponent.class)
        .ifPresent(
          pc -> {
            Point start = pc.position();
            System.out.println("DEBUG: Building base beam from " + start);
            Point end = calculateEndPoint(start, this.direction);
            int totalPoints = calculateNumberOfPoints(start, end);

            for (int i = 0; i < totalPoints; i++) {
              Entity segment = createNextSegment(start, end, totalPoints, i, this.direction);
              this.segments.add(segment);
            }
            System.out.println("DEBUG: Base beam has " + this.segments.size() + " segments.");
          });

      // Erweiterung zur Hauptliste hinzufügen
      if (!extendedSegments.isEmpty()) {
        System.out.println(
            "DEBUG: Adding " + extendedSegments.size() + " extended segments to main list.");
        segments.addAll(extendedSegments);
      }
      // Alle Segmente (Basis + Erweitert) zum Spiel hinzufügen
      System.out.println("DEBUG: Adding all " + segments.size() + " segments to the game.");
      segments.forEach(Game::add);

      updateEmitterVisual(true);
    }

    /** Deaktiviert die Lichtwand, entfernt alle ihre Segmente aus dem Spiel. */
    private void deactivate() {
      if (!active) return;
      this.active = false;
      System.out.println(
          "DEBUG: deactivate() called. Removing all " + segments.size() + " segments from game.");

      segments.forEach(Game::remove);
      segments.clear();
      System.out.println(
          "DEBUG: Main segment list cleared. Extended segment definition remains ("
              + extendedSegments.size()
              + " segments).");
      // extendedSegments bleiben als Definition für die nächste Aktivierung erhalten
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
    private Entity createNextSegment(
      Point from, Point to, int totalPoints, int currentIndex, Direction segmentDirection) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Point currentPoint = new Point(x, y);
      System.out.println(
          "DEBUG: Creating segment "
              + (currentIndex + 1)
              + "/"
              + totalPoints
              + " at "
              + currentPoint);
      PositionComponent pc = new PositionComponent(currentPoint);

      Entity segment = new Entity("lightWallSegment");
      segment.add(pc);

      segment.add(new CollideComponent()); // Fügt die Kollisionskomponente hinzu

      pc.rotation(rotationFor(segmentDirection));

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
   * Erweitert eine bestehende Lichtwand um einen zusätzlichen Strahl.
   *
   * @param wallEmitter Die Emitter-Entität der zu erweiternden Wand.
   * @param from Der Startpunkt des neuen Strahls.
   * @param direction Die Richtung des neuen Strahls.
   */
  public static void extendWall(Entity wallEmitter, Point from, Direction direction) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(c -> c.extend(from, direction));
  }

  /**
   * Entfernt den zusätzlichen Strahl von einer Lichtwand.
   *
   * @param wallEmitter Die Emitter-Entität der Wand.
   */
  public static void trimWall(Entity wallEmitter) {
    wallEmitter.fetch(LightWallComponent.class).ifPresent(LightWallComponent::trim);
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
