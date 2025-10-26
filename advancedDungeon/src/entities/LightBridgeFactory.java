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

  // Konfigurierbarer Spawn-Offset für Portal-Extend Start, falls Exit auf Wand liegt
  public static int spawnOffset = 1;

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
    private final List<Entity> extendedSegments = new ArrayList<>();
    private final Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

    private Point baseEnd = null;
    private Point extendEnd = null;

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
            }

            baseEnd = end;
            // Collider für Basis (und ggf. Extend) setzen
            Point finalEnd = pickFurtherEnd(start);
            createColliderForBridge(start, finalEnd, this.direction);

            // Alle Segmente ins Spiel + Pit schließen
            segments.forEach(segment -> {
              Game.add(segment);
              segment.fetch(PositionComponent.class).ifPresent(spc ->
                Game.tileAt(spc.position()).ifPresent(tile -> { if (tile instanceof PitTile pit) coverPit(pit); })
              );
            });
          });

      // Extend-Definition ins Spiel übernehmen, falls vorhanden
      if (!extendedSegments.isEmpty()) {
        extendedSegments.forEach(segment -> {
          Game.add(segment);
          segment.fetch(PositionComponent.class).ifPresent(spc ->
            Game.tileAt(spc.position()).ifPresent(tile -> { if (tile instanceof PitTile pit) coverPit(pit); })
          );
        });
        segments.addAll(extendedSegments);
      }

      updateEmitterVisual(true);
    }

    /**
     * Deaktiviert die Lichtbrücke, entfernt ihre Segmente und stellt den ursprünglichen Zustand der
     * Abgründe wieder her. Collider wird entfernt.
     */
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

      // Collider am Emitter entfernen
      owner.remove(CollideComponent.class);
    }

    /**
     * Erzeugt/aktualisiert Extend-Segmente. Bei aktiver Brücke werden sie sofort ins Spiel
     * übernommen und Pits geschlossen. Collider wird auf fernstes Ende gesetzt.
     */
    public void extend(Direction direction, Point from, PortalExtendComponent ignored) {
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
          createColliderForBridge(start, finalEnd, this.direction);
        });
      }
    }

    /** Entfernt Extend-Segmente. Stellt Pit-Zustände wieder her und setzt Collider auf Basis. */
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
          if (baseEnd != null) createColliderForBridge(pc.position(), baseEnd, this.direction);
        });
      }
      extendedSegments.clear();
      extendEnd = null;
    }

    /**
     * Aktualisiert die visuelle Darstellung des Emitters, um seinen aktiven oder inaktiven Zustand
     * widerzuspiegeln.
     */
    private void updateEmitterVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Normal.depth());
      owner.add(dc);
      owner.name(on ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");

      owner.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    }

    /**
     * Erstellt ein einzelnes Segment der Lichtbrücke. Pit-Schließen passiert beim Hinzufügen ins
     * Spiel, nicht hier.
     */
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

    /** Berechnet die Rotation für die Grafik basierend auf der Richtung. */
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
     * Erstellt/aktualisiert die CollideComponent für die Brücke am Emitter. Nicht solide.
     * Breite/Höhe und Offset hängen von Blickrichtung und Länge ab.
     */
    private void createColliderForBridge(Point start, Point end, Direction direction) {
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

    /**
     * Schließt einen PitTile stabil (mit Referenzzählung pro Tile).
     * Speichert den vorherigen Zustand, um ihn beim letzten Entfernen wiederherzustellen.
     */
    private void coverPit(PitTile pit) {
      Object[] state = coveredPits.get(pit);
      if (state == null) {
        boolean wasOpen = pit.isOpen();
        long prevT = pit.timeToOpen();
        // stabil schließen: Zeit auf groß setzen, damit close() nicht sofort wieder öffnet
        pit.timeToOpen(60 * 60 * 1000L); // >= 60s, damit Stable-Textur greift
        pit.close();
        coveredPits.put(pit, new Object[] {1, wasOpen, prevT});
      } else {
        int count = (Integer) state[0];
        state[0] = count + 1;
      }
    }

    /**
     * Öffnet (oder stellt) einen PitTile wieder her, wenn keine Segmente mehr darüber liegen.
     */
    private void uncoverPit(PitTile pit) {
      Object[] state = coveredPits.get(pit);
      if (state == null) {
        return; // nichts zu tun
      }
      int count = (Integer) state[0];
      if (count > 1) {
        state[0] = count - 1;
        return;
      }
      boolean wasOpen = (Boolean) state[1];
      long prevT = (Long) state[2];
      pit.timeToOpen(prevT);
      if (wasOpen) pit.open(); else pit.close();
      coveredPits.remove(pit);
    }

    // Wählt das fernere Ende (Basis oder Extend) entlang der Blickrichtung
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

  /**
   * Erstellt eine vollständige, steuerbare Lichtbrücke und gibt die Emitter-Entität zurück.
   */
  public static Entity createLightBridge(Point from, Direction direction, boolean active) {

    Entity emitter = LightBridgeComponent.createEmitterForBridge(from, direction);

    LightBridgeComponent bridgeComponent = new LightBridgeComponent(emitter, direction);
    emitter.add(bridgeComponent);

    // Portal-Extend-Unterstützung wie bei LightWall
    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend = (d, e, portalExtendComponent) -> {
      Point startPoint = e;
      Tile tileAtExit = Game.tileAt(startPoint).orElse(null);
      if (tileAtExit instanceof WallTile) {
        startPoint = startPoint.translate(d.scale(spawnOffset));
      }
      extendBridge(emitter, startPoint, d, portalExtendComponent);
    };
    pec.onTrim = (emitterEntity) -> trimBridge(emitter);
    emitter.add(pec);

    if (active) {
      emitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
    }

    Game.add(emitter);
    return emitter;
  }

  /** Aktiviert eine gegebene Lichtbrücke. */
  public static void activateBridge(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::activate);
  }

  /** Deaktiviert eine gegebene Lichtbrücke. */
  public static void deactivateBridge(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::deactivate);
  }

  /** Erweitert eine bestehende Lichtbrücke. */
  public static void extendBridge(Entity bridgeEmitter, Point from, Direction direction, PortalExtendComponent pec) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(c -> c.extend(direction, from, pec));
  }

  /** Entfernt die Extend-Segmente der Lichtbrücke. */
  public static void trimBridge(Entity bridgeEmitter) {
    bridgeEmitter.fetch(LightBridgeComponent.class).ifPresent(LightBridgeComponent::trim);
  }

  /**
   * Gibt eine schreibgeschützte Liste der Segment-Entitäten für eine gegebene Brücke zurück.
   */
  public static List<Entity> getBridgeSegments(Entity bridgeEmitter) {
    return bridgeEmitter
      .fetch(LightBridgeComponent.class)
      .map(LightBridgeComponent::getSegments)
      .orElse(Collections.emptyList());
  }

  /** Berechnet die Anzahl der Segmente, die benötigt werden, um die Distanz zu überbrücken. */
  private static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  /** Berechnet den Endpunkt einer Brücke, bis eine {@link WallTile} erreicht wird. */
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
