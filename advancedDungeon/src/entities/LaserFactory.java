package entities;

import contrib.components.CollideComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.WallTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import produsAdvanced.abstraction.portals.components.LaserComponent;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;

public class LaserFactory {

  private static final SimpleIPath LASER = new SimpleIPath("portal/laser");
  private static final SimpleIPath EMITTER_ACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_inactive.png");
  private static final SimpleIPath EMITTER_INACTIVE =
      new SimpleIPath("portal/laser/laser_emitter_inactive.png");

  private final Point from;
  private final Direction direction;

  public LaserFactory(Point from, Direction direction) {
    this.from = from;
    this.direction = direction;
  }

  public Entity create(boolean active) {
    Entity emitter = createEmitter(from, direction);
    LaserComponent comp = new LaserComponent(direction);
    emitter.add(comp);
    Game.add(emitter);
    if (active) {
      activate(emitter);
    }
    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend = comp::extend;
    pec.onTrim =
        (e) -> {
          comp.trim();
        };
    emitter.add(pec);

    return emitter;
  }

  public static Entity createLaser(Point from, Direction direction, boolean active) {
    return new LaserFactory(from, direction).create(active);
  }

  public static void activate(Entity emitter) {
    emitter
        .fetch(LaserComponent.class)
        .ifPresent(
            comp -> {
              if (comp.isActive()) return;

              emitter
                  .fetch(PositionComponent.class)
                  .ifPresent(
                      pc -> {
                        Point start = pc.position();
                        Direction dir = comp.getDirection();
                        Point end = calculateEndPoint(start, dir);
                        int totalPoints = calculateNumberOfPoints(start, end);

                        for (int i = 0; i < totalPoints; i++) {
                          Entity segment = createSegment(start, end, totalPoints, i, dir);
                          comp.getSegments().add(segment);
                          Game.add(segment);
                        }

                        updateEmitterVisual(emitter, true, dir);
                        configureEmitterHitbox(emitter, totalPoints, dir);
                        comp.setActive(true);
                      });
            });
  }

  public static void deactivate(Entity emitter) {
    emitter
        .fetch(LaserComponent.class)
        .ifPresent(
            comp -> {
              if (!comp.isActive()) return;

              comp.getSegments().forEach(Game::remove);
              comp.getSegments().clear();

              removeEmitterHitbox(emitter);
              updateEmitterVisual(emitter, false, comp.getDirection());
              comp.setActive(false);
            });
  }

  public static List<Entity> getSegments(Entity emitter) {
    return emitter
        .fetch(LaserComponent.class)
        .map(LaserComponent::getSegments)
        .orElse(Collections.emptyList());
  }

  static Entity createEmitter(Point position, Direction direction) {
    Entity emitter = new Entity("laserEmitter");
    PositionComponent pc = new PositionComponent(position);
    pc.rotation(rotationFor(direction));
    emitter.add(pc);

    DrawComponent dc = new DrawComponent(EMITTER_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);

    return emitter;
  }

  static Entity createSegment(Point from, Point to, int totalPoints, int index, Direction dir) {
    int denom = Math.max(totalPoints - 1, 1);
    float x = from.x() + index * (to.x() - from.x()) / denom;
    float y = from.y() + index * (to.y() - from.y()) / denom;
    Point p = new Point(x + 0.5f, y + 0.5f);

    Entity segment = new Entity("laserSegment");
    PositionComponent pc = new PositionComponent(p);
    pc.rotation(rotationFor(dir));
    segment.add(pc);

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(LASER);
    State idle = State.fromMap(animationMap, "vertical");
    StateMachine sm = new StateMachine(List.of(idle));
    DrawComponent dc = new DrawComponent(sm);
    segment.add(dc);

    return segment;
  }

  static void updateEmitterVisual(Entity emitter, boolean on, Direction direction) {
    DrawComponent dc = new DrawComponent(on ? EMITTER_ACTIVE : EMITTER_INACTIVE);
    dc.depth(DepthLayer.Normal.depth());
    emitter.add(dc);
    emitter.name(on ? "laserEmitterActive" : "laserEmitterInactive");
    emitter.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
  }

  static void configureEmitterHitbox(Entity emitter, int totalPoints, Direction dir) {
    float hitboxX = 1f;
    float hitboxY = 1f;
    float offsetX = 0.375f;
    float offsetY = 0.375f;

    switch (dir) {
      case LEFT -> {
        hitboxX = totalPoints;
        hitboxY = 0.25f;
        offsetX = (-totalPoints) + 1;
      }
      case RIGHT -> {
        hitboxX = totalPoints;
        hitboxY = 0.25f;
        offsetX = 0;
      }
      case UP -> {
        hitboxX = 0.25f;
        hitboxY = totalPoints;
        offsetY = 0;
      }
      case DOWN -> {
        hitboxX = 0.25f;
        hitboxY = totalPoints;
        offsetY = (-totalPoints) + 1;
      }
      default -> {}
    }

    CollideComponent cc =
        new CollideComponent(
            Vector2.of(offsetX, offsetY),
            Vector2.of(hitboxX, hitboxY),
            CollideComponent.DEFAULT_COLLIDER,
            (you, other, collisionDir) -> {});
    emitter.add(cc);
    emitter.fetch(CollideComponent.class).ifPresent(c -> c.isSolid(true));
  }

  static void removeEmitterHitbox(Entity emitter) {
    emitter.remove(CollideComponent.class);
  }

  static float rotationFor(Direction d) {
    return switch (d) {
      case UP -> 0f;
      case DOWN -> 180f;
      case LEFT -> 90f;
      case RIGHT -> -90f;
      default -> 0f;
    };
  }

  static int calculateNumberOfPoints(Point from, Point to) {
    float dx = Math.abs(to.x() - from.x());
    float dy = Math.abs(to.y() - from.y());
    return (int) Math.max(dx, dy) + 1;
  }

  static Point calculateEndPoint(Point from, Direction beamDirection) {
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

  public static void extendLaser(
      Direction direction,
      Point from,
      List<Entity> laserEntities,
      PortalExtendComponent pec,
      LaserComponent comp) {
    Point end = calculateEndPoint(from, direction);
    int totalPoints = calculateNumberOfPoints(from, end);

    for (int i = 0; i < totalPoints; i++) {
      Entity segment = createSegment(from, end, totalPoints, i, direction);
      laserEntities.add(segment);
      Game.add(segment);
    }

    Entity newEmitter = createEmitter(from, direction);
    newEmitter.add(comp);
    newEmitter.add(pec);
    newEmitter.remove(DrawComponent.class);

    configureEmitterHitbox(newEmitter, totalPoints, direction);
    comp.setActive(true);

    laserEntities.add(newEmitter);
    Game.add(newEmitter);
  }

  public static void trimAfterFirstEmitter(List<Entity> entities) {
    int firstEmitterIndex = -1;
    for (int i = 0; i < entities.size(); i++) {
      if ("laserEmitter".equals(entities.get(i).name())) {
        firstEmitterIndex = i;
        break;
      }
    }
    if (firstEmitterIndex != -1 && firstEmitterIndex + 1 < entities.size()) {
      List<Entity> toRemove = entities.subList(firstEmitterIndex + 1, entities.size());
      toRemove.forEach(Game::remove);
      toRemove.clear();
    }
  }
}
