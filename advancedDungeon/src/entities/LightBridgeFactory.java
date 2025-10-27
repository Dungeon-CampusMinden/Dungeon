package entities;

import contrib.components.CollideComponent;
import contrib.components.ProjectileComponent;
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
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;

/**
 * Factory und Verwaltung für „Lichtbrücken“ (Light Bridges).
 *
 * <p>Diese Klasse erzeugt Emitter-Entities für Lichtbrücken und stellt die öffentliche API bereit,
 * um Brücken zu aktivieren/deaktivieren und deren Länge über Portal-Events zu erweitern oder zu
 * kürzen. Die eigentliche Laufzeitlogik (Segmenterzeugung, Collider, Pit-Abdeckung) liegt in der
 * inneren Komponente LightBridgeComponent, die an den Emitter angehängt wird.
 */
public class LightBridgeFactory {

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH =
      new SimpleIPath("portal/light_bridge");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE =
      new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE =
      new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  /** Anzahl Kacheln, um die der Extend-/Spawn-Startpunkt vor dem Emitter versetzt wird. */
  public static int spawnOffset = 1;

  /** Cache für Segment-Animationen, um Mehrfachladen zu vermeiden. */
  private static Map<String, Animation> SEGMENT_ANIMATION_CACHE;

  /**
   * Liefert die gecachten Segment-Animationen. Beim ersten Zugriff werden die
   * Spritesheet-Animationen geladen und für weitere Aufrufe zwischengespeichert.
   *
   * @return Map der Animationen (Key entspricht dem State-Namen der Animation)
   */
  private static Map<String, Animation> segmentAnimations() {
    if (SEGMENT_ANIMATION_CACHE == null) {
      SEGMENT_ANIMATION_CACHE = Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
    }
    return SEGMENT_ANIMATION_CACHE;
  }

  /**
   * Komponente, die den Zustand und die Segmente einer einzelnen Lichtbrücke verwaltet.
   *
   * <p>Aufgaben: - erzeugt/entfernt Segmente beim Aktivieren/Deaktivieren, - erweitert/kürzt
   * Segmente dynamisch (Extend/Trim), - passt einen nicht-soliden Collider an die aktuelle
   * Brückenlänge an, - deckt PitTiles ab und stellt deren Zustand bei Entfernung wieder her, -
   * sorgt für korrekte Ausrichtung und Darstellung des Emitters. Die Interaktion nach außen erfolgt
   * über die Factory-Methoden in LightBridgeFactory.
   */
  public static class LightBridgeComponent implements Component {
    private final Entity owner;
    private final Direction direction;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    private final List<Entity> extendedSegments = new ArrayList<>();
    private final java.util.Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

    private Point baseEnd = null;
    private Point extendEnd = null;

    /**
     * Erstellt die Verwaltungs-Komponente für eine Lichtbrücke.
     *
     * @param owner Emitter-Entity, an die sich die Brücke bindet
     * @param direction Ausrichtung der Brücke (Richtung der Segment-Erzeugung)
     */
    public LightBridgeComponent(Entity owner, Direction direction) {
      this.owner = owner;
      this.direction = direction;
    }

    /**
     * Deckt eine Grube (PitTile) ab, sofern das übergebene Segment auf einer solchen Kachel liegt.
     * Dabei wird die Öffnungszeit extrem verlängert und der aktuelle Zustand gesichert, um ihn bei
     * Entfernung wiederherstellen zu können.
     *
     * @param segment Segment-Entity, dessen Kachel geprüft wird
     */
    private void coverPitIfNeeded(Entity segment) {
      segment
          .fetch(PositionComponent.class)
          .flatMap(spc -> Game.tileAt(spc.position()))
          .filter(tile -> tile instanceof PitTile)
          .map(tile -> (PitTile) tile)
          .ifPresent(this::coverPit);
    }

    /**
     * Gibt eine zuvor abgedeckte Grube wieder frei, sofern das übergebene Segment auf einer PitTile
     * lag. Der ursprüngliche Zustand der Grube wird gemäß Zähler wiederhergestellt.
     *
     * @param segment Segment-Entity, dessen Kachel geprüft wird
     */
    private void uncoverPitIfNeeded(Entity segment) {
      segment
          .fetch(PositionComponent.class)
          .flatMap(spc -> Game.tileAt(spc.position()))
          .filter(tile -> tile instanceof PitTile)
          .map(tile -> (PitTile) tile)
          .ifPresent(this::uncoverPit);
    }

    /**
     * Erstellt einen Emitter an der angegebenen Position, richtet ihn gemäß Richtung aus und setzt
     * die inaktive Darstellung. Zusätzlich wird ein harmloser ProjectileComponent hinzugefügt,
     * damit externe Systeme den Emitter als Projektil-Ziel ignorieren.
     *
     * @param from Startposition (Tile-Koordinaten)
     * @param direction Ausrichtung des Emitters und der späteren Brücke
     * @return der erstellte Emitter-Entity
     */
    public static Entity createEmitter(Point from, Direction direction) {
      Entity emitter = new Entity("lightBridgeEmitter");
      PositionComponent pc = new PositionComponent(from);
      pc.rotation(rotationFor(direction));
      emitter.add(pc);

      DrawComponent dc = new DrawComponent(EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      emitter.add(dc);

      emitter.add(new ProjectileComponent(new Point(0, 0), new Point(0, 0), Vector2.ZERO, e -> {}));

      return emitter;
    }

    /**
     * Aktiviert die Brücke: erzeugt Basissegmente vom Emitter bis zur nächsten Wand, deckt
     * betroffene PitTiles ab und legt einen nicht-soliden Collider über die gesamte Länge. Doppelte
     * Aktivierungen werden ignoriert.
     */
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
                for (int i = 0; i < total; i++)
                  segments.add(createNextSegment(start, end, total, i, this.direction));
                baseEnd = end;
                createColliderForBeam(start, pickFurtherEnd(start), this.direction);
              });

      segments.addAll(extendedSegments);
      segments.forEach(Game::add);
      segments.forEach(this::coverPitIfNeeded);
      updateEmitterVisual(true);
    }

    /**
     * Deaktiviert die Brücke: entfernt alle Segmente, gibt ggf. PitTiles frei, setzt die inaktive
     * Darstellung und entfernt den Collider. Mehrfache Aufrufe sind idempotent.
     */
    private void deactivate() {
      if (!active) return;
      active = false;
      segments.forEach(this::uncoverPitIfNeeded);
      segments.forEach(Game::remove);
      segments.clear();
      updateEmitterVisual(false);
      owner.remove(CollideComponent.class);
    }

    /**
     * Erzeugt zusätzliche Segmente in angegebener Richtung ab Position "from" und erweitert die
     * Brücke temporär über die Basislänge hinaus. Falls bereits eine Erweiterung existiert, wird
     * zuvor getrimmt. Bei aktiver Brücke werden Segmente und Collider sofort in die Spielwelt
     * übernommen.
     *
     * @param direction Richtung der Erweiterung
     * @param from Startpunkt der Erweiterung (Tile-Koordinaten)
     */
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
        extendedSegments.forEach(this::coverPitIfNeeded);
        segments.addAll(extendedSegments);
        owner
            .fetch(PositionComponent.class)
            .ifPresent(
                pc ->
                    createColliderForBeam(
                        pc.position(), pickFurtherEnd(pc.position()), this.direction));
      }
    }

    /**
     * Entfernt zuvor erzeugte Erweiterungssegmente und stellt die Collider-Länge auf die Basis
     * zurück. Bei inaktiver Brücke wird lediglich der interne Zustand bereinigt.
     */
    public void trim() {
      if (extendedSegments.isEmpty()) return;
      if (active) {
        extendedSegments.forEach(this::uncoverPitIfNeeded);
        extendedSegments.forEach(Game::remove);
        segments.removeAll(extendedSegments);
        owner
            .fetch(PositionComponent.class)
            .ifPresent(
                pc -> {
                  if (baseEnd != null)
                    createColliderForBeam(pc.position(), baseEnd, this.direction);
                });
      }
      extendedSegments.clear();
      extendEnd = null;
    }

    /**
     * Aktualisiert die Darstellung und Metadaten des Emitters abhängig vom Aktivitätszustand
     * (Texture, Depth-Layer, Name, Rotation).
     *
     * @param on true, wenn aktiv; false, wenn inaktiv
     */
    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      owner.add(dc);
      owner.name(on ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");
      owner.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    }

    /**
     * Erzeugt ein einzelnes Segment-Entity auf der Strecke von "from" nach "to" und richtet es aus.
     * Die Position wird anhand der Anzahl totalPoints und des Index currentIndex gleichmäßig
     * verteilt.
     *
     * @param from Startposition der Strecke
     * @param to Endposition der Strecke
     * @param totalPoints Gesamtanzahl der zu verteilenden Punkte (inklusive Endpunkte)
     * @param currentIndex Index des aktuell zu erzeugenden Segments [0..totalPoints-1]
     * @param rotDir Ausrichtung des Segments für die Rotation
     * @return das erzeugte Segment-Entity
     */
    private Entity createNextSegment(
        Point from, Point to, int totalPoints, int currentIndex, Direction rotDir) {
      float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
      float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);
      Entity segment = new Entity("lightBridgeSegment");
      segment.add(new PositionComponent(new Point(x, y)));
      segment.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(rotDir)));

      AnimationConfig cfg = segmentAnimations().get("idle").getConfig();
      State idle = new State("idle", SEGMENT_SPRITESHEET_PATH, cfg);
      StateMachine sm = new StateMachine(List.of(idle));
      DrawComponent dc = new DrawComponent(sm);
      dc.depth(DepthLayer.Ground.depth());
      segment.add(dc);
      return segment;
    }

    /**
     * Liefert die Rotation in Grad für eine Richtung. 0° entspricht UP, 180° DOWN, 90° LEFT und
     * -90° RIGHT.
     *
     * @param d Richtung
     * @return Rotation in Grad für die Darstellung
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
     * Erstellt oder aktualisiert den nicht-soliden Collider des Emitters so, dass er die aktuelle
     * Brückenlänge abdeckt. Breite/Höhe und Offsets werden abhängig von der Ausrichtung berechnet
     * und schließen immer die Position des Emitters mit ein.
     *
     * @param start Startpunkt (Emitterposition)
     * @param end Endpunkt der aktuellen Brücke
     * @param dir Ausrichtung der Brücke
     */
    private void createColliderForBeam(Point start, Point end, Direction dir) {
      float width = 1f, height = 1f, offsetX = 0f, offsetY = 0f;
      if (dir == Direction.LEFT || dir == Direction.RIGHT) {
        float len = Math.abs(end.x() - start.x()) + 1f;
        width = Math.max(1f, len);
        offsetX = (dir == Direction.LEFT) ? -(width - 1f) : 0f;
      } else if (dir == Direction.UP || dir == Direction.DOWN) {
        float len = Math.abs(end.y() - start.y()) + 1f;
        height = Math.max(1f, len);
        offsetY = (dir == Direction.DOWN) ? -(height - 1f) : 0f;
      }

      CollideComponent cc =
          new CollideComponent(
              Vector2.of(offsetX, offsetY),
              Vector2.of(width, height),
              CollideComponent.DEFAULT_COLLIDER,
              (a, b, c) -> {});
      cc.isSolid(false);
      owner.remove(CollideComponent.class);
      owner.add(cc);
    }

    /**
     * Erhöht die Abdeckungszählung für eine Grube und konserviert deren ursprünglichen Zustand
     * (offen/geschlossen, Zeit bis zum Öffnen). Beim ersten Abdecken wird die Grube geschlossen und
     * die Öffnungszeit weit in die Zukunft gesetzt.
     *
     * @param pit Grubenkachel
     */
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

    /**
     * Verringert die Abdeckungszählung einer Grube. Erreicht der Zähler 0, wird der zuvor
     * gespeicherte Zustand (Zeit bis zum Öffnen, offen/geschlossen) wiederhergestellt und die Grube
     * aus der Verwaltung entfernt.
     *
     * @param pit Grubenkachel
     */
    private void uncoverPit(PitTile pit) {
      Object[] state = coveredPits.get(pit);
      if (state == null) return;
      int count = (Integer) state[0];
      if (count > 1) {
        state[0] = count - 1;
        return;
      }
      boolean wasOpen = (Boolean) state[1];
      long prevT = (Long) state[2];
      pit.timeToOpen(prevT);
      if (wasOpen) pit.open();
      else pit.close();
      coveredPits.remove(pit);
    }

    /**
     * Wählt den aktuell am weitesten entfernten Endpunkt der Brücke relativ zum Emitter aus.
     *
     * <p>Es werden zwei Kandidaten betrachtet: - baseEnd: das Ende der Basisbrücke (vom Emitter bis
     * zur nächsten Wand), - extendEnd: das Ende einer temporären Erweiterung (falls vorhanden).
     *
     * <p>Nullwerte werden gegen den übergebenen Startpunkt ersetzt. Abhängig von der
     * Brückenrichtung wird der weiter „in Richtung des Strahls“ liegende Punkt gewählt: - RIGHT:
     * größeres x gewinnt, - LEFT: kleineres x gewinnt, - UP: größeres y gewinnt, - DOWN: kleineres
     * y gewinnt. Bei Gleichstand oder unbekannter Richtung wird baseEnd bevorzugt.
     *
     * @param start Ausgangspunkt (typisch: Emitterposition), Fallback falls kein Endpunkt bekannt
     *     ist
     * @return der weiter entfernte Endpunkt in Strahlrichtung; niemals null
     */
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

    /**
     * Berechnet die Anzahl von diskreten Kachelpunkten zwischen zwei Koordinaten inklusive beider
     * Endpunkte. Basis für die gleichmäßige Segmentverteilung.
     *
     * @param from Startpunkt
     * @param to Endpunkt
     * @return Anzahl der Punkte (mindestens 1)
     */
    private int calculateNumberOfPoints(Point from, Point to) {
      float dx = Math.abs(to.x() - from.x());
      float dy = Math.abs(to.y() - from.y());
      return (int) Math.max(dx, dy) + 1;
    }

    /**
     * Ermittelt den letzten freien Kachelpunkt in beamDirection, beginnend bei from, bevor eine
     * Wandkachel (WallTile) erreicht wird. Trifft die nächste Kachel bereits auf eine Wand, wird
     * der aktuelle Punkt zurückgegeben.
     *
     * @param from Startpunkt
     * @param beamDirection Richtung des Strahls
     * @return letzter freier Punkt vor einer Wand (niemals null, solange from im Level liegt)
     */
    private Point calculateEndPoint(Point from, Direction beamDirection) {
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

  /**
   * Erzeugt eine neue Lichtbrücke samt Emitter. Optional kann die Brücke direkt aktiviert werden,
   * wodurch Segmente, Collider und Pit-Abdeckung sofort erstellt werden. Außerdem wird ein
   * PortalExtendComponent registriert, der Extend/Trim-Ereignisse verarbeitet.
   *
   * @param from Startposition des Emitters (Tile-Koordinaten)
   * @param direction Ausrichtung der Lichtbrücke
   * @param active true, wenn die Brücke sofort aktiv sein soll
   * @return der Emitter-Entity der Lichtbrücke
   */
  public static Entity createLightBridge(Point from, Direction direction, boolean active) {
    Entity emitter = LightBridgeComponent.createEmitter(from, direction);
    LightBridgeComponent bridgeComponent = new LightBridgeComponent(emitter, direction);
    emitter.add(bridgeComponent);

    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend =
        (d, e, portalExtendComponent) -> {
          Point startPoint = e.translate(d.scale(spawnOffset));
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

  /**
   * Aktiviert die Lichtbrücke eines gegebenen Emitters, falls vorhanden.
   *
   * @param wallEmitter Emitter-Entity
   */
  public static void activate(Entity wallEmitter) {
    wallEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
  }

  /**
   * Deaktiviert die Lichtbrücke eines gegebenen Emitters, falls vorhanden.
   *
   * @param wallEmitter Emitter-Entity
   */
  public static void deactivate(Entity wallEmitter) {
    wallEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::deactivate);
  }

  /**
   * Interne Helper-Methode: erweitert die Brücke eines Emitters ab einem Startpunkt in gegebener
   * Richtung.
   *
   * @param wallEmitter Emitter
   * @param from Startpunkt der Erweiterung
   * @param direction Richtung der Erweiterung
   */
  private static void extendWall(Entity wallEmitter, Point from, Direction direction) {
    wallEmitter.fetch(LightBridgeComponent.class).ifPresent(c -> c.extend(direction, from));
  }

  /**
   * Interne Helper-Methode: nimmt eine zuvor ausgeführte Erweiterung wieder zurück.
   *
   * @param wallEmitter Emitter
   */
  private static void trimWall(Entity wallEmitter) {
    wallEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::trim);
  }
}
