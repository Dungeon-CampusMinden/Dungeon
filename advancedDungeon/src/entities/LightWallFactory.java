package entities;

import contrib.components.CollideComponent;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LightWallFactory – ähnlich LightBridgeFactory, aber Segmente sind solide (Blockade für Spieler & Projektile).
 * Funktionalität:
 *  - Inaktiver Emitter (andere Textur)
 *  - Aktivieren: erzeugt solide Lasersegnente bis zum ersten nicht passierbaren Tile
 *  - Deaktivieren: entfernt Segmente
 */
public final class LightWallFactory {
  //private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_wall_sprite");
  //private static final SimpleIPath EMITTER_TEXTURE_ACTIVE = new SimpleIPath("portal/light_wall_emitter/light_wall_emitter.png");
  //private static final SimpleIPath EMITTER_TEXTURE_INACTIVE = new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_inactive.png");

  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_wall");
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE = new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_active.png");
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE = new SimpleIPath("portal/light_wall_emitter/light_wall_emitter_inactive.png");

  private static List<core.utils.components.draw.state.State> SEGMENT_STATES;
  private static core.utils.components.draw.state.State DEFAULT_SEGMENT_STATE;

  private static final int MAX_RANGE = 512;

  // Entferne zentrale ACTIVE_SEGMENTS Map zugunsten komponenteninternem State (Legacy-Kompatibilität optional)
  private static final Map<Entity, List<Entity>> ACTIVE_SEGMENTS = new ConcurrentHashMap<>(); // legacy tracking

  public static class WallSurfaceComponent implements Component {}
  public static class LightWallEmitterComponent implements Component {
    private final Entity owner;
    private boolean active = false;
    private final List<Entity> segments = new ArrayList<>();
    public LightWallEmitterComponent(Entity owner){ this(owner,false); }
    public LightWallEmitterComponent(Entity owner, boolean initialActive){
      this.owner = owner;
      if (initialActive) doActivate(); else updateVisual(false);
    }
    public boolean isActive(){ return active; }
    public void activate(){ if (!active) doActivate(); }
    public void deactivate(){ if (active) doDeactivate(); }
    public void toggle(){ if (active) deactivate(); else activate(); }
    private void doActivate(){
      if (ACTIVE_SEGMENTS.containsKey(owner)) return; // legacy guard
      owner.fetch(PositionComponent.class).ifPresent(pc -> {
        List<Entity> built = buildSegments(pc.position(), pc.viewDirection());
        built.forEach(Game::add);
        segments.addAll(built);
        ACTIVE_SEGMENTS.put(owner, built);
      });
      active = true;
      updateVisual(true);
    }
    private void doDeactivate(){
      List<Entity> list = new ArrayList<>(segments);
      for (Entity seg : list) Game.remove(seg);
      segments.clear();
      ACTIVE_SEGMENTS.remove(owner);
      active = false;
      updateVisual(false);
    }
    private void updateVisual(boolean on){
      DrawComponent dc = new DrawComponent(on ? EMITTER_TEXTURE_ACTIVE : EMITTER_TEXTURE_INACTIVE);
      dc.depth(DepthLayer.Level.depth());
      owner.add(dc);
      owner.name(on ? "lightWallEmitter" : "lightWallEmitterInactive");
    }
  }

  /* ===================== Öffentliche API ===================== */

  public static Entity createInactiveEmitter(Point p, Direction dir) {
    return createEmitter(p, dir, false);
  }

  /** Erzeugt einen Wall-Emitter mit gewünschtem Aktiv-Status. */
  public static Entity createEmitter(Point p, Direction dir, boolean active) {
    PositionComponent pc = new PositionComponent(p, dir);
    pc.rotation(rotationFor(dir));
    Entity e = new Entity(active ? "lightWallEmitter" : "lightWallEmitterInactive");
    e.add(pc);
    e.add(new CollideComponent());
    e.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(true));
    e.add(new LightWallEmitterComponent(e, active));
    return e;
  }

  public static Entity createWallEmitter(Point p, Direction dir, boolean active) {
    return createEmitter(p, dir, active);
  }

  public static void activateEmitter(Entity emitter) {
    if (emitter == null) return;
    emitter.fetch(LightWallEmitterComponent.class).ifPresent(LightWallEmitterComponent::activate);
  }

  public static void deactivateEmitter(Entity emitter) {
    if (emitter == null) return;
    emitter.fetch(LightWallEmitterComponent.class).ifPresent(LightWallEmitterComponent::deactivate);
  }

  public static boolean isEmitterActive(Entity emitter) {
    return emitter != null && emitter.fetch(LightWallEmitterComponent.class).map(LightWallEmitterComponent::isActive).orElse(false);
  }

  /* ===================== Intern ===================== */

  private static List<Entity> buildSegments(Point from, Direction dir) {
    List<Entity> segments = new ArrayList<>();
    Point current = from.translate(dir);

    for (int i = 0; i < MAX_RANGE; i++) {
      Tile tile = Game.tileAt(current).orElse(null);
      if (tile == null) break; // außerhalb Level
      LevelElement el = tile.levelElement();
      boolean isPit = (el == LevelElement.HOLE || el == LevelElement.PIT);
      boolean passable = el.value() || isPit; // Pits & Holes jetzt durchlässig für Wand-Ausbreitung
      if (!passable) break;
      Entity seg = buildSegment(current);
      segments.add(seg);
      current = current.translate(dir);
    }
    return segments;
  }

  private static Entity buildSegment(Point p) {
    Entity e = new Entity("lightWallSegment");
    e.add(new PositionComponent(p));
    DrawComponent dc = buildSegmentDrawComponent();
    dc.depth(DepthLayer.Level.depth()); // über dem Boden
    e.add(dc);
    e.add(new CollideComponent());
    e.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(true));
    e.add(new WallSurfaceComponent());
    return e;
  }

  private static DrawComponent buildSegmentDrawComponent() {
    if (SEGMENT_STATES == null) {
      Map<String, core.utils.components.draw.animation.Animation> map =
          core.utils.components.draw.animation.Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
      SEGMENT_STATES = new ArrayList<>();
      for (var entry : map.entrySet()) {
        SEGMENT_STATES.add(new core.utils.components.draw.state.State(entry.getKey(), entry.getValue()));
      }
      if (SEGMENT_STATES.isEmpty()) throw new IllegalStateException("Spritesheet light_wall_sprite leer.");
      DEFAULT_SEGMENT_STATE = SEGMENT_STATES.get(0);
    }
    var sm = new core.utils.components.draw.state.StateMachine(SEGMENT_STATES, DEFAULT_SEGMENT_STATE);
    return new DrawComponent(sm);
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
