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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for creating tractor beam entities between two points.
 *
 * <p>The factory interpolates positions between a start point ({@code from}) and an end point
 * ({@code to}), creating a sequence of entities that visually represent a continuous tractor beam.
 * The Beam is only one entity with a huge hitbox and can apply a pulling force to other entities it
 * collides with, based on the beam's direction.
 *
 * <p>Alternatively, a tractor beam can also be created by specifying only a start point and a
 * direction. In this case, the beam will extend from the start point until it hits the next wall.
 *
 * <p>The following transport directions use the following beam colors:
 *
 * <p>horizontal to the right --> blue
 *
 * <p>horizontal to the left --> red
 *
 * <p>vertical down --> blue
 *
 * <p>vertical up --> red
 */
public class NewLightBridgeFactory {

  private static final float forceMagnitude = 20f;

  /** Pfad zum Spritesheet der Brückensegmente. */
  private static final SimpleIPath SEGMENT_SPRITESHEET_PATH = new SimpleIPath("portal/light_bridge");
  /** Feinabstimmung der Segment-Animationsgeschwindigkeit (kleiner = schneller) */
  private static final int BRIDGE_FRAMES_PER_SPRITE = 4;
  /** Textur des aktiven Emitters. */
  private static final SimpleIPath EMITTER_TEXTURE_ACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_active.png");
  /** Textur des inaktiven Emitters. */
  private static final SimpleIPath EMITTER_TEXTURE_INACTIVE = new SimpleIPath("portal/light_bridge_emitter/light_bridge_emitter_inactive.png");

  /** Speicher für die von dieser Brücke abgedeckten Pits und deren Ursprungszustand. */
  private final Map<PitTile, Object[]> coveredPits = new ConcurrentHashMap<>();

  private final Point from;
  private final Point to;
  private final int totalPoints;
  private int currentIndex = 0;
  private final Direction beamDirection;


  /**
   * Creates a new {@code TractorBeamFactory} for generating tractor beam entities between the
   * specified points.
   *
   * @param from the starting point of the tractor beam
   * @param to the end point of the tractor beam
   */
  public NewLightBridgeFactory(Point from, Point to) {
    this.from = from;
    this.to = to;
    this.totalPoints = calculateNumberOfPoints(from, to);
    this.beamDirection = calculateDirection(from, to);
  }

  /**
   * Creates a new {@code TractorBeamFactory} for generating tractor beam entities from one specific
   * point into a direction until a wall is in the way.
   *
   * @param from the starting point of the tractor beam
   * @param beamDirection the direction the beam is emitted to
   */
  public NewLightBridgeFactory(Point from, Direction beamDirection) {
    this.from = from;
    this.to = calculateEndPoint(from, beamDirection);
    this.totalPoints = calculateNumberOfPoints(from, to);
    this.beamDirection = beamDirection;
  }

  /**
   * Calculates the number of points (entities) between {@code from} and {@code to}.
   *
   * @param from the starting point
   * @param to the end point
   * @return the number of interpolated points, including both start and end
   */
  private int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  /**
   * Determines the main direction from {@code from} to {@code to}.
   *
   * @param from the starting point
   * @param to the end point
   * @return the primary direction (horizontal or vertical) toward {@code to}
   */
  private Direction calculateDirection(Point from, Point to) {
    float dx = to.x() - from.x();
    float dy = to.y() - from.y();

    if (Math.abs(dx) > Math.abs(dy)) {
      return dx > 0 ? Direction.RIGHT : Direction.LEFT;
    } else if (Math.abs(dy) > 0) {
      return dy > 0 ? Direction.UP : Direction.DOWN;
    } else {
      return Direction.NONE;
    }
  }

  /**
   * Determines the last available point for a tractor beam. The beam is stopped by a wallTile, so
   * the EndPoint has to be last point in front of a wall tile.
   *
   * @param from the starting point
   * @param beamDirection the emitted direction of the tractor beam
   * @return the last available point
   */
  private Point calculateEndPoint(Point from, Direction beamDirection) {
    Point lastPoint = from;
    Point currentPoint = from;
    Tile currentTile = Game.tileAt(from).orElse(null);
    while (currentTile != null && !currentTile.getClass().equals(WallTile.class)) {
      lastPoint = currentPoint;
      currentPoint = currentPoint.translate(beamDirection);
      currentTile = Game.tileAt(currentPoint).orElse(null);
    }
    return lastPoint;
  }

  /**
   * Checks whether more entities can be generated.
   *
   * @return {@code true} if additional entities are available, {@code false} otherwise
   */
  private boolean hasNext() {
    return currentIndex < totalPoints;
  }

  /**
   * Deckt eine Grube (Pit) ab. Der Ursprungszustand wird gespeichert und die Grube geschlossen.
   * @param pit Die abzudeckende Grube.
   */
  private void coverPit(PitTile pit) {
    // Nur hinzufügen, wenn es noch nicht von dieser Brücke abgedeckt wird.
    coveredPits.computeIfAbsent(pit, k -> {
      boolean wasOpen = pit.isOpen();
      long originalTime = pit.timeToOpen();
      if (wasOpen) {
        // Setzt eine sehr lange Zeit, damit es nicht von selbst wieder öffnet.
        if (originalTime == 0) pit.timeToOpen(3_600_000L);
        pit.close();
      }
      return new Object[]{wasOpen, originalTime};
    });
  }

  /**
   * Hebt die Abdeckung für eine Grube auf und stellt den ursprünglichen Zustand wieder her.
   * @param pit Die aufzudeckende Grube.
   */
  private void uncoverPit(PitTile pit) {
    Object[] originalState = coveredPits.remove(pit);
    if (originalState != null) {
      boolean wasOpen = (boolean) originalState[0];
      long originalTimeToOpen = (long) originalState[1];

      pit.timeToOpen(originalTimeToOpen);
      if (wasOpen) {
        pit.open();
      } else {
        pit.close();
      }
    }
  }

  /**
   * Creates the next entity of the tractor beam.
   *
   * <p>This entity is one part of the visual representation for the whole beam.
   *
   * @return a new tractor beam entity, or {@code null} if no more entities can be created
   */


  private Entity createNextEntity(Direction direction) {
    if (!hasNext()) {
      return null;
    }

    // Interpolated position between from and to
    float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
    float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);

    Entity lightBridge = new Entity("lightBridge");
    PositionComponent pc = new PositionComponent(new Point(x, y));

    // Prüfen, ob die Kachel ein Pit ist und dieses abdecken
    Game.tileAt(new Point(x, y)).ifPresent(tile -> {
      if (tile instanceof PitTile pit) {
        this.coverPit(pit);
      }
    });

    pc.rotation(rotationFor(direction));
    lightBridge.add(pc);

    // Spritesheet laden und einfachen Idle-State verwenden
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(SEGMENT_SPRITESHEET_PATH);
    State idle = State.fromMap(animationMap, "idle");
    StateMachine sm = new StateMachine(List.of(idle));

    DrawComponent dc = new DrawComponent(sm);
    dc.depth(DepthLayer.Ground.depth());
    lightBridge.add(dc);

    currentIndex++;
    return lightBridge;
  }

  /**
   * Creates a complete tractor beam between two points by generating all entities.
   *
   * @param from the starting point of the beam
   * @param to the end point of the beam
   * @return a list of all tractor beam entities
   */
  public static List<Entity> createLightBridge(Point from, Point to) {
    NewLightBridgeFactory factory = new NewLightBridgeFactory(from, to);

    List<Entity> lightBridgeEntities = new ArrayList<>();

    Direction direction = factory.calculateDirection(from, to);
    lightBridgeEntities.add(factory.createBridgeEmitter(from, direction));

    while (factory.hasNext()) {
      lightBridgeEntities.add(factory.createNextEntity(direction));
    }

    return lightBridgeEntities;
  }

  /**
   * Creates a light bridge. It only needs a spawn point and an emitted direction. The beam is
   * stopped by the next wall.
   *
   * @param from the starting point of the beam
   * @param direction the emitted direction of the tractor beam
   * @return a list of all tractor beam entities
   */
  public static void createLightBridge(Point from, Direction direction, boolean active) {
    NewLightBridgeFactory factory = new NewLightBridgeFactory(from, direction);
    List<Entity> lightBridgeEntities = new ArrayList<>();

    lightBridgeEntities.add(factory.createBridgeEmitter(from, direction));

    if (active) {
      while (factory.hasNext()) {
        lightBridgeEntities.add(factory.createNextEntity(direction));
      }
    }

    lightBridgeEntities.forEach(Game::add);
  }

  /**
   * Creates the entity representing the beam emitter. The emitters function is to represent the
   * start point and hitbox of the beam.
   *
   * <p>The hitbox and offset of the beam are dependent on the number ob tractor beam entities and
   * the direction.
   *
   * @param p the spawn point of the entity
   * @param dir the direction the beam is emitted to
   * @return the beam emitter entity
   */
  public Entity createBridgeEmitter(Point p, Direction dir) {
    PositionComponent pc = new PositionComponent(p, dir);
    pc.rotation(rotationFor(dir));
    Entity e = new Entity("lightBridgeEmitter");
    //Entity e = new Entity(active ? "lightBridgeEmitter" : "lightBridgeEmitterInactive");
    e.add(pc);
    e.add(new CollideComponent());
    e.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(true));
    DrawComponent dc = new DrawComponent(EMITTER_TEXTURE_INACTIVE);
    dc.depth(DepthLayer.AbovePlayer.depth());
    e.add(dc);

    return e;
  }




  private float rotationFor(Direction d) {
    return switch (d) {
      case UP -> 0f;
      case DOWN -> 180f;
      case LEFT -> 90f;
      case RIGHT -> -90f;
      default -> 0f;
    };
  }

}
