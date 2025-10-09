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
 * LightBridgeFactory – Minimal-API
 *
 * Verantwortlichkeiten:
 *  - Erzeugt einen permanenten (inaktiven) Emitter.
 *  - Aktiviert: wechselt Texture + legt Segmente an (inkl. temporärem Schließen von Pits).
 *  - Deaktiviert: entfernt Segmente + stellt Pits wieder her + Texture zurück.
 *
 * Öffentliche Methoden:
 *  - createInactiveEmitter(Point, Direction)
 *  - activateEmitter(Entity)
 *  - deactivateEmitter(Entity)
 *  - isEmitterActive(Entity)
 */
public final class LightBridgeFactory {
  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_bridge");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  private static List<core.utils.components.draw.state.State> SEGMENT_STATES;
  private static core.utils.components.draw.state.State DEFAULT_SEGMENT_STATE;

  private static final int MAX_RANGE = 512;

  private record PitState(boolean wasOpen, long originalTimeToOpen) {}
  private static final Map<PitTile, PitState> COVERED_PITS = new ConcurrentHashMap<>();
  private static final Map<Entity, List<Entity>> ACTIVE_SEGMENTS = new ConcurrentHashMap<>();

  private LightBridgeFactory() {}

  public static class BridgeSurfaceComponent implements Component {}

  public static class LightBridgeEmitterComponent implements Component {
    private final Entity owner;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    public LightBridgeEmitterComponent(Entity owner) { this(owner, false); }
    public LightBridgeEmitterComponent(Entity owner, boolean initialActive) {
      this.owner = owner;
      if (initialActive) {
        doActivate();
      } else {
        updateVisual(false);
      }
    }
    public boolean isActive() { return active; }
    public void activate() { if (!active) { doActivate(); } }
    public void deactivate() { if (active) { doDeactivate(); } }
    private void doActivate() {
      if (ACTIVE_SEGMENTS.containsKey(owner)) return; // legacy guard
      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        List<Entity> built = buildSegments(pc.position(), pc.viewDirection());
        built.forEach(Game::add);
        segments.addAll(built);
        ACTIVE_SEGMENTS.put(owner, built); // legacy tracking
      });
      active = true;
      updateVisual(true);
    }
    private void doDeactivate() {
      List<Entity> list = new ArrayList<>(segments);
      for (Entity seg : list) {
        if ("lightBridgeSegment".equals(seg.name())) {
          seg.fetch(PositionComponent.class)
              .ifPresent(pc -> Game.tileAt(pc.position()).ifPresent(t -> { if (t instanceof PitTile pit) reopenPit(pit); }));
        }
        Game.remove(seg);
      }
      segments.clear();
      ACTIVE_SEGMENTS.remove(owner); // legacy
      repairCoveredPits();
      active = false;
      updateVisual(false);
    }
    private void updateVisual(boolean on) {
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Level.depth());
      owner.add(dc);
      owner.name(on ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");

      // Preserve the rotation from the PositionComponent
      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        Direction dir = pc.viewDirection();
        float rotation;

        // Handle rotation differently based on direction
        // For LEFT/RIGHT, we need to add 180 degrees to flip the direction
        // For UP/DOWN, we need to use the original rotation values
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
   * Erzeugt einen inaktiven Emitter (Legacy-Methode).
   */
  public static Entity createInactiveEmitter(Point p, Direction dir) {
    return createEmitter(p, dir, false);
  }

  /**
   * Erzeugt einen Emitter mit gewünschtem Aktiv-Status und liefert die Entity-Instanz zurück.
   * Der Aufrufer kann diese Referenz speichern und später über activateEmitter(entity) bzw.
   * deactivateEmitter(entity) gezielt genau diesen Emitter schalten.
   */
  public static Entity createEmitter(Point p, Direction dir, boolean active) {
    PositionComponent pc = new PositionComponent(p, dir);
    pc.rotation(rotationFor(dir));
    Entity e = new Entity(active ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");
    e.add(pc);
    // Draw erfolgt in Component via updateVisual / doActivate -> hier nur Platzhalter wenn nötig
    e.add(new CollideComponent());
    e.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(true));
    e.add(new LightBridgeEmitterComponent(e, active));
    return e;
  }

  /**
   * Convenience-Name (frühere Methode lieferte spezialisierte Subklasse). Behält API für Aufrufe bei,
   * gibt aber nun einfach das normale Entity zurück (kein extends Entity mehr nötig).
   */
  public static Entity createBridgeEmitter(Point p, Direction dir, boolean active){
    return createEmitter(p, dir, active);
  }

  public static void activateEmitter(Entity emitter) {
    if (emitter == null) return;
    emitter.fetch(LightBridgeEmitterComponent.class).ifPresent(LightBridgeEmitterComponent::activate);
  }

  public static void deactivateEmitter(Entity emitter) {
    if (emitter == null) return;
    emitter.fetch(LightBridgeEmitterComponent.class).ifPresent(LightBridgeEmitterComponent::deactivate);
  }

  public static boolean isEmitterActive(Entity emitter) {
    return emitter != null && emitter.fetch(LightBridgeEmitterComponent.class).map(LightBridgeEmitterComponent::isActive).orElse(false);
  }

  /* ===================== Interne Helfer ===================== */

  private static List<Entity> buildSegments(Point from, Direction dir) {
    List<Entity> segments = new ArrayList<>();
    Point current = from.translate(dir);

    for (int i = 0; i < MAX_RANGE; i++) {
      Tile tile = Game.tileAt(current).orElse(null);
      if (tile == null) break; // außerhalb Level
      LevelElement el = tile.levelElement();
      boolean isPit = (el == LevelElement.HOLE || el == LevelElement.PIT);
      boolean passable = el.value() || isPit;
      if (!passable) break;
      // Die Richtung 'dir' wird nun an buildSegment übergeben
      Entity seg = buildSegment(current, dir);
      segments.add(seg);
      if (isPit && tile instanceof PitTile pit) closePitIfNeeded(pit);
      current = current.translate(dir);
    }
    return segments;
  }

  private static Entity buildSegment(Point p, Direction dir) {
    Entity e = new Entity("lightBridgeSegment");
    PositionComponent pc = new PositionComponent(p);
    pc.rotation(rotationFor(dir));
    e.add(pc);
    // Die Richtung 'dir' wird an buildSegmentDrawComponent übergeben
    DrawComponent dc = buildSegmentDrawComponent(dir);
    dc.depth(DepthLayer.Ground.depth());
    e.add(dc);
    e.add(new BridgeSurfaceComponent());
    return e;
  }

  private static DrawComponent buildSegmentDrawComponent(Direction dir) {
    // Die Logik wird nur einmal ausgeführt, um die Animationen zu laden.
    if (SEGMENT_STATES == null) {
      Map<String, core.utils.components.draw.animation.Animation> map =
        core.utils.components.draw.animation.Animation.loadAnimationSpritesheet(
          SEGMENT_SPRITESHEET_PATH);
      SEGMENT_STATES = new ArrayList<>();
      for (var entry : map.entrySet()) {
        SEGMENT_STATES.add(
          new core.utils.components.draw.state.State(entry.getKey(), entry.getValue()));
      }
      if (SEGMENT_STATES.isEmpty())
        throw new IllegalStateException("Spritesheet light_bridge_sprite leer.");
      DEFAULT_SEGMENT_STATE = SEGMENT_STATES.get(0);
    }

    // Create a new list of states with new Animation instances to avoid shared animation state
    List<core.utils.components.draw.state.State> newStates = new ArrayList<>();
    for (core.utils.components.draw.state.State originalState : SEGMENT_STATES) {
      // Create a new Animation with a modified configuration for faster animation
      core.utils.components.draw.animation.Animation originalAnim = originalState.getAnimation();
      // Get the original config and create a new one with reduced framesPerSprite
      core.utils.components.draw.animation.AnimationConfig originalConfig = originalAnim.getConfig();
      core.utils.components.draw.animation.AnimationConfig newConfig = new core.utils.components.draw.animation.AnimationConfig();
      // Copy all settings but reduce framesPerSprite to make animation faster
      if (originalConfig.config().isPresent()) {
        newConfig.config(originalConfig.config().get());
      }
      newConfig.scaleX(originalConfig.scaleX())
               .scaleY(originalConfig.scaleY())
               .isLooping(originalConfig.isLooping())
               .centered(originalConfig.centered())
               // Reduce framesPerSprite to make animation faster (divide by 2)
               .framesPerSprite(4);

      core.utils.components.draw.animation.Animation newAnim =
        new core.utils.components.draw.animation.Animation(
          SEGMENT_SPRITESHEET_PATH,
          newConfig);

      // Create a new State with the new Animation
      newStates.add(new core.utils.components.draw.state.State(originalState.name, newAnim));
    }

    // Find the appropriate state for this direction
    core.utils.components.draw.state.State activeState = null;
    String stateName = (dir == Direction.LEFT || dir == Direction.RIGHT) ? "horizontal" : "vertical";

    for (core.utils.components.draw.state.State s : newStates) {
      if (s.name.toLowerCase().contains(stateName)) {
        activeState = s;
        break;
      }
    }

    // If no matching state was found, use the first state
    if (activeState == null && !newStates.isEmpty()) {
      activeState = newStates.get(0);
    }

    var sm = new core.utils.components.draw.state.StateMachine(newStates, activeState);
    return new DrawComponent(sm);
  }

  /* ===================== Pit-Verwaltung ===================== */

  private static void closePitIfNeeded(PitTile pit) {
    if (COVERED_PITS.containsKey(pit)) return;
    boolean wasOpen = pit.isOpen();
    long orig = pit.timeToOpen();
    if (wasOpen) {
      if (orig == 0) pit.timeToOpen(3_600_000L);
      pit.close();
    }
    COVERED_PITS.put(pit, new PitState(wasOpen, orig));
  }

  private static void reopenPit(PitTile pit) {
    PitState st = COVERED_PITS.remove(pit);
    if (st == null) return;
    pit.timeToOpen(st.originalTimeToOpen());
    if (st.wasOpen()) pit.open(); else pit.close();
  }

  private static void repairCoveredPits() {
    if (COVERED_PITS.isEmpty()) return;
    for (PitTile pit : new ArrayList<>(COVERED_PITS.keySet())) reopenPit(pit);
  }

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
