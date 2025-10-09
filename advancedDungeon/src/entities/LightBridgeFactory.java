package entities;

import contrib.components.CollideComponent;
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
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Erzeugt und steuert Lichtbrücken-Emitter sowie deren Segmente.
 *
 * Verantwortlichkeiten:
 * - Erzeugen eines Emitters (inaktiv oder aktiv startend)
 * - Aktivieren: erstellt sichtbare, begehbare Brücken-Segmente und schließt Pits temporär
 * - Deaktivieren: entfernt Segmente und stellt Pits in ihren Ursprungszustand zurück
 *
 * Hinweise:
 * - Dies ist KEINE Persistenz-Klasse (keine JPA-Entity). Es werden daher Javadoc-Kommentare
 *   statt JPA-Annotationen verwendet.
 */
public final class LightBridgeFactory {
  /** Pfad zum Spritesheet der Brückensegmente. */
  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_bridge");
  /** Textur des aktiven Emitters. */
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  /** Textur des inaktiven Emitters. */
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  /** Zwischengespeicherte States der Segment-Animationen (Spritesheet wird einmalig geladen). */
  private static List<core.utils.components.draw.state.State> SEGMENT_STATES;

  /** Maximale Ausbreitungsreichweite der Brücke in Tiles. */
  private static final int MAX_RANGE = 512;

  /**
   * Interner Zustand temporär geschlossener Pits, damit beim Deaktivieren der Ursprungszustand
   * wiederhergestellt werden kann.
   */
  private record PitState(boolean wasOpen, long originalTimeToOpen) {}

  /**
   * Referenzgezählte Abdeckung von Pits über mehrere Emitter hinweg.
   * Nur wenn der Zähler 0 wird, wird der Ursprungszustand wiederhergestellt.
   */
  private static final class PitCover {
    final PitState original;
    int count;
    PitCover(PitState original, int count) { this.original = original; this.count = count; }
  }

  /** Globaler Speicher für abgedeckte Pits mit Referenzzählung. */
  private static final Map<PitTile, PitCover> PIT_COVERS = new ConcurrentHashMap<>();

  private LightBridgeFactory() {}

  /**
   * Marker-Komponente für Brücken-Oberfläche. Ermöglicht Systems, Segmente zu erkennen.
   */
  public static class BridgeSurfaceComponent implements Component {}

  /**
   * Komponente am Emitter, verwaltet Aktivierungsstatus und zugehörige Segmente.
   */
  public static class LightBridgeEmitterComponent implements Component {
    private final Entity owner;           // Emitter-Entity
    private boolean active = false;       // aktueller Aktivstatus
    private final List<Entity> segments = new ArrayList<>(); // aktuell erzeugte Segmente

    /**
     * Erstellt eine Komponente mit initial inaktivem Emitter.
     * @param owner Emitter-Entity
     */
    public LightBridgeEmitterComponent(Entity owner) { this(owner, false); }

    /**
     * Erstellt eine Komponente und setzt den initialen Aktivstatus.
     * @param owner Emitter-Entity
     * @param initialActive true, wenn der Emitter direkt aktiv sein soll
     */
    public LightBridgeEmitterComponent(Entity owner, boolean initialActive) {
      this.owner = owner;
      if (initialActive) {
        doActivate();
      } else {
        updateVisual(false);
      }
    }

    /**
     * @return true, wenn der Emitter aktuell aktiv ist.
     */
    public boolean isActive() { return active; }

    /**
     * Aktiviert den Emitter und erzeugt Segmente, falls noch nicht aktiv.
     */
    public void activate() { if (!active) { doActivate(); } }

    /**
     * Deaktiviert den Emitter und entfernt Segmente, falls aktuell aktiv.
     */
    public void deactivate() { if (active) { doDeactivate(); } }

    // Führt die eigentliche Aktivierung aus, erzeugt Segmente entlang der Blickrichtung
    private void doActivate() {
      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        List<Entity> built = buildSegments(pc.position(), pc.viewDirection());
        built.forEach(Game::add);
        segments.addAll(built);
      });
      active = true;
      updateVisual(true);
    }

    // Führt die eigentliche Deaktivierung aus, entfernt Segmente und repariert nur eigene Pits
    private void doDeactivate() {
      List<Entity> list = new ArrayList<>(segments);
      for (Entity seg : list) {
        if ("lightBridgeSegment".equals(seg.name())) {
          seg.fetch(PositionComponent.class)
              .ifPresent(pc -> Game.tileAt(pc.position()).ifPresent(t -> {
                if (t instanceof PitTile pit) uncoverPit(pit);
              }));
        }
        Game.remove(seg);
      }
      segments.clear();
      active = false;
      updateVisual(false);
    }

    // Aktualisiert die Optik (aktive/inaktive Textur) und übernimmt die Rotation
    private void updateVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Level.depth());
      owner.add(dc);
      owner.name(on ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");

      // Rotation aus der Blickrichtung übernehmen
      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        Direction dir = pc.viewDirection();
        float rotation;
        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
          rotation = rotationFor(dir) + 180f;
        } else {
          rotation = rotationFor(dir);
        }
        pc.rotation(rotation);
      });
    }
  }

  /* ===================== Öffentliche API ===================== */

  /**
   * Erzeugt einen inaktiven Emitter.
   * @param p Startposition
   * @param dir initiale Blickrichtung
   * @return neu angelegte Emitter-Entity (zunächst inaktiv)
   */
  public static Entity createInactiveEmitter(Point p, Direction dir) {
    return createEmitter(p, dir, false);
  }

  /**
   * Erzeugt einen Emitter mit gewünschtem Aktiv-Status und liefert die Entity-Instanz zurück.
   * Der Aufrufer kann diese Referenz speichern und später über activateEmitter(entity) bzw.
   * deactivateEmitter(entity) gezielt genau diesen Emitter schalten.
   * @param p Startposition des Emitters
   * @param dir Blickrichtung des Emitters
   * @param active true, wenn der Emitter direkt aktiv sein soll
   * @return die erzeugte Emitter-Entity
   */
  public static Entity createEmitter(Point p, Direction dir, boolean active) {
    PositionComponent pc = new PositionComponent(p, dir);
    pc.rotation(rotationFor(dir));
    Entity e = new Entity(active ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");
    e.add(pc);
    e.add(new CollideComponent());
    e.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(true));
    e.add(new LightBridgeEmitterComponent(e, active));
    return e;
  }

  /**
   * Veraltete Convenience-Methode; bitte {@link #createEmitter(Point, Direction, boolean)} verwenden.
   */
  @Deprecated
  public static Entity createBridgeEmitter(Point p, Direction dir, boolean active){
    return createEmitter(p, dir, active);
  }

  /**
   * Aktiviert einen Emitter, falls vorhanden.
   * @param emitter die Emitter-Entity
   */
  public static void activateEmitter(Entity emitter) {
    if (emitter == null) return;
    emitter.fetch(LightBridgeEmitterComponent.class).ifPresent(LightBridgeEmitterComponent::activate);
  }

  /**
   * Deaktiviert einen Emitter, falls vorhanden.
   * @param emitter die Emitter-Entity
   */
  public static void deactivateEmitter(Entity emitter) {
    if (emitter == null) return;
    emitter.fetch(LightBridgeEmitterComponent.class).ifPresent(LightBridgeEmitterComponent::deactivate);
  }

  /**
   * @param emitter die Emitter-Entity
   * @return true, wenn der Emitter aktiv ist; sonst false
   */
  public static boolean isEmitterActive(Entity emitter) {
    return emitter != null && emitter.fetch(LightBridgeEmitterComponent.class).map(LightBridgeEmitterComponent::isActive).orElse(false);
  }

  /* ===================== Interne Helfer ===================== */

  /**
   * Erzeugt Brücken-Segmente ab einer Startposition in angegebener Richtung,
   * bis ein nicht-passierbares Tile erreicht wird oder die Maximalreichweite erreicht ist.
   * Pits werden dabei temporär geschlossen.
   * @param from Startposition (Emitter-Position)
   * @param dir Ausbreitungsrichtung
   * @return Liste erzeugter Segment-Entities (noch nicht im Game registriert)
   */
  private static List<Entity> buildSegments(Point from, Direction dir) {
    List<Entity> segments = new ArrayList<>();
    Point current = from.translate(dir);

    for (int i = 0; i < MAX_RANGE; i++) {
      Tile tile = Game.tileAt(current).orElse(null);
      if (tile == null) break; // außerhalb des Levels
      LevelElement el = tile.levelElement();
      boolean isPit = (el == LevelElement.HOLE || el == LevelElement.PIT);
      boolean passable = el.value() || isPit;
      if (!passable) break;

      // Segment für aktuelle Kachel erzeugen
      Entity seg = buildSegment(current, dir);
      segments.add(seg);

      // Pits referenzgezählt abdecken
      if (isPit && tile instanceof PitTile pit) coverPit(pit);

      current = current.translate(dir);
    }
    return segments;
  }

  /**
   * Baut ein einzelnes Brücken-Segment an Position p und richtet es nach dir aus.
   * @param p Zielposition
   * @param dir Ausrichtung
   * @return Segment-Entity
   */
  private static Entity buildSegment(Point p, Direction dir) {
    Entity e = new Entity("lightBridgeSegment");

    Point pos = new Point(p.x(), p.y());
    PositionComponent pc = new PositionComponent(pos);
    pc.rotation(rotationFor(dir));
    e.add(pc);

    DrawComponent dc = buildSegmentDrawComponent(dir);
    dc.depth(DepthLayer.Ground.depth());
    e.add(dc);
    e.add(new BridgeSurfaceComponent());
    return e;
  }

  /**
   * Erstellt einen DrawComponent für ein Segment in Abhängigkeit der Ausrichtung.
   * Das Spritesheet wird einmalig geladen; pro Segment werden neue Animationen erzeugt,
   * um keinen Animationszustand zwischen Segmenten zu teilen.
   * @param dir gewünschte Ausrichtung (horizontal/vertikal)
   * @return DrawComponent mit passender Animation
   */
  private static DrawComponent buildSegmentDrawComponent(Direction dir) {
    // Spritesheet nur einmal laden
    if (SEGMENT_STATES == null) {
      Map<String, core.utils.components.draw.animation.Animation> map =
        core.utils.components.draw.animation.Animation.loadAnimationSpritesheet(
          SEGMENT_SPRITESHEET_PATH);
      if (map == null || map.isEmpty()) {
        throw new IllegalStateException("Spritesheet light_bridge_sprite leer.");
      }
      SEGMENT_STATES = new ArrayList<>();
      for (var entry : map.entrySet()) {
        SEGMENT_STATES.add(
          new core.utils.components.draw.state.State(entry.getKey(), entry.getValue()));
      }
    }

    // Pro Segment neue States/Animationen erzeugen, um geteilten Animationszustand zu vermeiden
    List<core.utils.components.draw.state.State> newStates = new ArrayList<>();
    for (core.utils.components.draw.state.State originalState : SEGMENT_STATES) {
      core.utils.components.draw.animation.Animation originalAnim = originalState.getAnimation();
      core.utils.components.draw.animation.AnimationConfig originalConfig = originalAnim.getConfig();
      core.utils.components.draw.animation.AnimationConfig newConfig = new core.utils.components.draw.animation.AnimationConfig();
      if (originalConfig.config().isPresent()) {
        newConfig.config(originalConfig.config().get());
      }
      newConfig
          .scaleX(originalConfig.scaleX())
          .scaleY(originalConfig.scaleY())
          .isLooping(originalConfig.isLooping())
          // schnellere Animation
          .framesPerSprite(4);

      core.utils.components.draw.animation.Animation newAnim =
        new core.utils.components.draw.animation.Animation(
          SEGMENT_SPRITESHEET_PATH,
          newConfig);

      newStates.add(new core.utils.components.draw.state.State(originalState.name, newAnim));
    }

    // Passenden State anhand der Ausrichtung wählen
    core.utils.components.draw.state.State activeState = null;
    String stateName = (dir == Direction.LEFT || dir == Direction.RIGHT) ? "horizontal" : "vertical";
    for (core.utils.components.draw.state.State s : newStates) {
      if (s.name.toLowerCase().contains(stateName)) {
        activeState = s;
        break;
      }
    }
    if (activeState == null && !newStates.isEmpty()) {
      activeState = newStates.get(0);
    }

    var sm = new core.utils.components.draw.state.StateMachine(newStates, activeState);
    return new DrawComponent(sm);
  }

  /* ===================== Pit-Verwaltung ===================== */

  /**
   * Deckt ein Pit ab (referenzgezählt). Beim ersten Cover wird der Ursprungszustand gemerkt
   * und das Pit geschlossen. Weitere Cover erhöhen nur den Zähler.
   */
  private static void coverPit(PitTile pit) {
    PIT_COVERS.compute(pit, (k, v) -> {
      if (v == null) {
        boolean wasOpen = pit.isOpen();
        long orig = pit.timeToOpen();
        if (wasOpen) {
          if (orig == 0) pit.timeToOpen(3_600_000L); // groß, damit es nicht von selbst öffnet
          pit.close();
        }
        return new PitCover(new PitState(wasOpen, orig), 1);
      } else {
        v.count += 1;
        return v;
      }
    });
  }

  /**
   * Hebt eine Abdeckung für ein Pit auf (referenzgezählt). Erst wenn keine Emitter mehr das Pit
   * abdecken (Zähler==0), wird der ursprüngliche Zustand wiederhergestellt.
   */
  private static void uncoverPit(PitTile pit) {
    PIT_COVERS.computeIfPresent(pit, (k, v) -> {
      v.count -= 1;
      if (v.count <= 0) {
        // Ursprungszustand wiederherstellen
        pit.timeToOpen(v.original.originalTimeToOpen());
        if (v.original.wasOpen()) pit.open(); else pit.close();
        return null; // Eintrag entfernen
      }
      return v;
    });
  }

  /**
   * Liefert die Rotation in Grad für eine Blickrichtung.
   * @param d Blickrichtung
   * @return Rotation in Grad
   */
  private static float rotationFor(Direction d) {
    return switch (d) {
      case UP -> 0f;
      case DOWN -> 180f;
      case LEFT -> -90f;
      case RIGHT -> 90f;
      default -> 0f;
    };
  }
}
