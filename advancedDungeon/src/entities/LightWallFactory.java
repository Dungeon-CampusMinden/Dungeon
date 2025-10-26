package entities;

import contrib.components.CollideComponent;
import core.utils.Vector2;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;
import produsAdvanced.abstraction.portals.components.PortalComponent;
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
import produsAdvanced.abstraction.portals.components.TractorBeamComponent;

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

  // Konfigurierbarer Spawn-Offset für Teleport-/Extend-Startpunkte vor dem Portal
  public static int spawnOffset = 1;

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

    // Merker für Collider-Längen: Basis und Extend
    private Point baseEnd = null;
    private Point extendEnd = null;

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
    public void extend(Direction direction, Point from, PortalExtendComponent portalExtendComponent) {
      trim();
      System.out.println("DEBUG [LWC.extend]: Called with direction=" + direction + ", from=" + from);
      System.out.println("DEBUG [LWC.extend]: Current active state: " + active);
      System.out.println("DEBUG [LWC.extend]: Current segments.size(): " + segments.size());

      Point end = calculateEndPoint(from, direction);
      System.out.println("DEBUG [LWC.extend]: Calculated end point: " + end);

      int totalPoints = calculateNumberOfPoints(from, end);
      System.out.println("DEBUG [LWC.extend]: Total points to create: " + totalPoints);

      for (int i = 0; i < totalPoints; i++) {
        Entity segment = createNextSegment(from, end, totalPoints, i, direction);
        this.extendedSegments.add(segment);
        System.out.println("DEBUG [LWC.extend]: Created segment " + (i+1) + "/" + totalPoints + " at " + segment.fetch(PositionComponent.class).map(pc -> pc.position()).orElse(null));
      }

      // Merke Extend-Ende für Collider-Berechnung
      extendEnd = end;

      System.out.println("DEBUG [LWC.extend]: Extended segments created: " + extendedSegments.size());

      if (active) {
        System.out.println("DEBUG [LWC.extend]: Wall is ACTIVE, adding segments to game immediately.");
        extendedSegments.forEach(e -> {
          System.out.println("DEBUG [LWC.extend]: Adding entity " + e.name() + " to game");
          Game.add(e);
        });
        segments.addAll(extendedSegments);
        System.out.println("DEBUG [LWC.extend]: Total segments after extend: " + segments.size());

        // Collider auf neue Gesamtlänge anpassen (Basis bis zum fernsten Ende in Blickrichtung)
        owner.fetch(PositionComponent.class).ifPresent(pc -> {
          Point start = pc.position();
          Point finalEnd = pickFurtherEnd(start);
          createColliderForBeam(start, finalEnd, this.direction);
        });
      } else {
        System.out.println("DEBUG [LWC.extend]: Wall is INACTIVE, segments will be added on next activation.");
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

        // Collider wieder auf Basis zurücksetzen
        owner.fetch(PositionComponent.class).ifPresent(pc -> {
          if (baseEnd != null) {
            createColliderForBeam(pc.position(), baseEnd, this.direction);
          }
        });
      }
      extendedSegments.clear();
      extendEnd = null;
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

            // Basis-Ende für Collider speichern
            baseEnd = end;

            // Collider für Basis/Extend erstellen (nimmt fernstes Ende, falls Extend vorhanden)
            Point finalEnd = pickFurtherEnd(start);
            createColliderForBeam(start, finalEnd, this.direction);
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

      // Collider am Emitter entfernen
      owner.remove(CollideComponent.class);

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

      //segment.add(new CollideComponent());

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

    /**
     * Erstellt/aktualisiert eine CollideComponent für den gesamten Strahl (Basis + ggf. Extend)
     * basierend auf Start- und Endpunkt in Blickrichtung.
     *
     * @param start Startpunkt (Emitter-Position)
     * @param end Endpunkt (letztes Segment in Blickrichtung)
     * @param direction Die Richtung des Strahls
     */
    private void createColliderForBeam(Point start, Point end, Direction direction) {
      // Dimensionen und Offsets relativ zur Emitter-Position berechnen
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
        default -> {
          width = 1f; height = 1f; offsetX = 0f; offsetY = 0f;
        }
      }

      CollideComponent cc = new CollideComponent(
        Vector2.of(offsetX, offsetY),
        Vector2.of(width, height),
        CollideComponent.DEFAULT_COLLIDER,
        (a, b, c) -> {}
      );

      owner.remove(CollideComponent.class); // Alten Collider entfernen
      owner.add(cc);

      System.out.println("DEBUG: Collider created/updated - width=" + width + ", height=" + height +
        ", offset=(" + offsetX + "," + offsetY + ") from=" + start + " to=" + end + ", dir=" + direction);
    }

    // Wählt das fernere Ende (Basis oder Extend) entlang der Blickrichtung
    private Point pickFurtherEnd(Point start) {
      Point candidateBase = (baseEnd != null) ? baseEnd : start;
      Point candidateExt = (extendEnd != null) ? extendEnd : candidateBase;
      switch (direction) {
        case RIGHT -> {
          return (candidateExt.x() > candidateBase.x()) ? candidateExt : candidateBase;
        }
        case LEFT -> {
          return (candidateExt.x() < candidateBase.x()) ? candidateExt : candidateBase;
        }
        case UP -> {
          return (candidateExt.y() > candidateBase.y()) ? candidateExt : candidateBase;
        }
        case DOWN -> {
          return (candidateExt.y() < candidateBase.y()) ? candidateExt : candidateBase;
        }
        default -> {
          return candidateBase;
        }
      }
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

    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend = (d,e,portalExtendComponent) -> {
      // Startpunkt am Exit-Portal prüfen und ggf. um spawnOffset in Blickrichtung verschieben
      Point startPoint = e;
      Tile tileAtExit = Game.tileAt(startPoint).orElse(null);
      if (tileAtExit instanceof WallTile) {
        startPoint = startPoint.translate(d.scale(spawnOffset));
      }
      extendWall(emitter, startPoint, d, portalExtendComponent);
    };
    pec.onTrim = (emitterEntity) -> {
      System.out.println("DEBUG [onTrim]: Called for entity " + emitterEntity.name());
      trimWall(emitter);
    };
    emitter.add(pec);

    // Workaround für PortalExtendSystem
    //emitter.add(new TractorBeamComponent(direction, from, new ArrayList<>()));

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
  public static void extendWall(Entity wallEmitter, Point from, Direction direction, PortalExtendComponent pec) {
    System.out.println("DEBUG [extendWall]: Called with wallEmitter=" + wallEmitter.name());
    System.out.println("DEBUG [extendWall]: Has LightWallComponent? " + wallEmitter.isPresent(LightWallComponent.class));

    wallEmitter
      .fetch(LightWallComponent.class)
      .ifPresentOrElse(
        c -> {
          System.out.println("DEBUG [extendWall]: LightWallComponent found, calling extend...");
          c.extend(direction, from, pec);
          System.out.println("DEBUG [extendWall]: extend() returned");
        },
        () -> System.out.println("DEBUG [extendWall]: LightWallComponent NOT FOUND!")
      );

    System.out.println("Extend called from factory");
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

    while (true) {
      Tile currentTile = Game.tileAt(currentPoint).orElse(null);

      // Strahl stoppt, wenn er außerhalb des Levels ist
      if (currentTile == null) {
        break;
      }

      // Prüfen, ob sich auf dem aktuellen Tile eine Wand befindet
      boolean isWall = currentTile instanceof WallTile;

      // Strahl stoppt an einer Wand
      if (isWall) {
        break;
      }

      lastPoint = currentPoint;
      currentPoint = currentPoint.translate(beamDirection);
    }
    return lastPoint;
  }
}
