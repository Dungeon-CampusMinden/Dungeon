package entities;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.WallTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;
import produsAdvanced.abstraction.portals.components.TractorBeamComponent;

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
public class TractorBeamFactory {

  private static final SimpleIPath TRACTOR_BEAM = new SimpleIPath("portal/tractor_beam");
  private static final SimpleIPath BEAM_EMITTER = new SimpleIPath("portal/beam_emitter");
  private static final float forceMagnitude = 20f;

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
  public TractorBeamFactory(Point from, Point to) {
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
  public TractorBeamFactory(Point from, Direction beamDirection) {
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
   * Creates the next entity of the tractor beam.
   *
   * <p>This entity is one part of the visual representation for the whole beam.
   *
   * @return a new tractor beam entity, or {@code null} if no more entities can be created
   */
  private Entity createNextEntity() {
    if (!hasNext()) {
      return null;
    }

    // Interpolated position between from and to
    float x = from.x() + currentIndex * (to.x() - from.x()) / (totalPoints - 1);
    float y = from.y() + currentIndex * (to.y() - from.y()) / (totalPoints - 1);

    Entity tractorBeam = new Entity("tractorBeam");
    tractorBeam.add(new PositionComponent(new Point(x, y)));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(TRACTOR_BEAM);

    DrawComponent dc = null;

    if (beamDirection.equals(Direction.RIGHT) || beamDirection.equals(Direction.LEFT)) {
      State stNormalHorizontal = State.fromMap(animationMap, "blue_horizontal");
      State stReversedHorizontal = State.fromMap(animationMap, "red_horizontal");
      StateMachine sm = new StateMachine(Arrays.asList(stNormalHorizontal, stReversedHorizontal));
      sm.addTransition(stNormalHorizontal, "reverse_horizontal", stReversedHorizontal);
      sm.addTransition(stReversedHorizontal, "normal_horizontal", stNormalHorizontal);
      dc = new DrawComponent(sm, DepthLayer.Ground);
    } else if (beamDirection.equals(Direction.UP) || beamDirection.equals(Direction.DOWN)) {
      State stNormalVertical = State.fromMap(animationMap, "blue_vertical");
      State stReversedVertical = State.fromMap(animationMap, "red_vertical");
      StateMachine sm = new StateMachine(Arrays.asList(stNormalVertical, stReversedVertical));
      sm.addTransition(stNormalVertical, "reverse_vertical", stReversedVertical);
      sm.addTransition(stReversedVertical, "normal_vertical", stNormalVertical);
      dc = new DrawComponent(sm, DepthLayer.Ground);
    }
    if (dc == null) {
      throw new IllegalArgumentException("Tractor Beam has no draw components");
    }
    tractorBeam.add(dc);

    switch (beamDirection) {
      case Direction.LEFT:
        dc.sendSignal("reverse_horizontal");
        break;
      case Direction.UP:
        dc.sendSignal("reverse_vertical");
        break;
    }

    currentIndex++;
    return tractorBeam;
  }

  /**
   * Creates a tractor beam. It only needs a spawn point and an emitted direction. The beam is
   * stopped by the next wall.
   *
   * @param from the starting point of the beam
   * @param direction the emitted direction of the tractor beam
   * @return a list of all tractor beam entities
   */
  public static Entity createTractorBeam(Point from, Direction direction) {
    TractorBeamFactory factory = new TractorBeamFactory(from, direction);
    List<Entity> tractorBeamEntities = new ArrayList<>();

    while (factory.hasNext()) {
      tractorBeamEntities.add(factory.createNextEntity());
    }
    Entity beamEmitter = factory.createBeamEmitter(from, direction);

    tractorBeamEntities.add(beamEmitter);
    TractorBeamComponent tbc = new TractorBeamComponent(direction, from, tractorBeamEntities);
    beamEmitter.add(tbc);
    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend =
        (d, e, portalExtendComponent) -> {
          tbc.extend(d, e, portalExtendComponent);
        };
    pec.onTrim =
        (e) -> {
          tbc.trim();
        };
    beamEmitter.add(pec);

    return beamEmitter;
  }

  /**
   * Creates the entity representing the beam emitter. The emitters function is to represent the
   * start point and hitbox of the beam.
   *
   * <p>The hitbox and offset of the beam are dependent on the number ob tractor beam entities and
   * the direction.
   *
   * @param spawnPoint the spawn point of the entity
   * @param direction the direction the beam is emitted to
   * @return the beam emitter entity
   */
  public Entity createBeamEmitter(Point spawnPoint, Direction direction) {
    Entity beamEmitter = new Entity("beamEmitter");
    beamEmitter.add(new PositionComponent(spawnPoint));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(BEAM_EMITTER);
    StateMachine sm;
    float hitboxX = 1f;
    float hitboxY = 1f;
    float offsetX = 0f;
    float offsetY = 0f;

    switch (direction) {
      case Direction.LEFT:
        State right = State.fromMap(animationMap, "right");
        sm = new StateMachine(List.of(right));
        beamEmitter.add(new DrawComponent(sm));
        hitboxX = currentIndex;
        offsetX = (-currentIndex) + 1;
        break;
      case Direction.UP:
        State bottom = State.fromMap(animationMap, "bottom");
        sm = new StateMachine(List.of(bottom));
        beamEmitter.add(new DrawComponent(sm));
        hitboxY = currentIndex;
        break;
      case Direction.RIGHT:
        State left = State.fromMap(animationMap, "left");
        sm = new StateMachine(List.of(left));
        beamEmitter.add(new DrawComponent(sm));
        hitboxX = currentIndex;
        break;
      case Direction.DOWN:
        State top = State.fromMap(animationMap, "top");
        sm = new StateMachine(List.of(top));
        beamEmitter.add(new DrawComponent(sm));
        hitboxY = currentIndex;
        offsetY = (-currentIndex) + 1;
        break;
    }

    TriConsumer<Entity, Entity, Direction> action =
        (you, other, collisionDir) -> {
          other
              .fetch(VelocityComponent.class)
              .ifPresent(
                  vc -> {
                    if (!other.isPresent(FlyComponent.class)) {
                      other.add(new FlyComponent());
                    }
                    Vector2 forceVector =
                        Vector2.of(
                            beamDirection.x() * forceMagnitude, beamDirection.y() * forceMagnitude);
                    vc.applyForce("beamEmitter", forceVector);
                  });
        };

    TriConsumer<Entity, Entity, Direction> actionLeave =
        (you, other, collisionDir) -> {
          other.remove(FlyComponent.class);
        };

    beamEmitter.add(
        new CollideComponent(
            Vector2.of(offsetX, offsetY),
            Vector2.of(hitboxX, hitboxY),
            CollideComponent.DEFAULT_COLLIDER,
            actionLeave));
    beamEmitter
        .fetch(CollideComponent.class)
        .ifPresent(
            cc -> {
              cc.onHold(action);
              cc.isSolid(false);
            });

    return beamEmitter;
  }

  /**
   * Reverses a tractor beam. Changes the force direction and color.
   *
   * @param tractorBeamEntities The list of all entities building the tractor beam
   */
  public static void reverseTractorBeam(List<Entity> tractorBeamEntities) {
    final Direction[] directionHolder = {Direction.NONE};
    for (Entity tractorBeamEntity : tractorBeamEntities) {
      if (tractorBeamEntity.name().equals("tractorBeam")) {
        tractorBeamEntity
            .fetch(DrawComponent.class)
            .ifPresent(
                dc -> {
                  String currentState = dc.currentStateName();

                  if (currentState.contains("horizontal")) {
                    if (currentState.startsWith("blue")) {
                      dc.sendSignal("reverse_horizontal");
                      directionHolder[0] = Direction.RIGHT;
                    } else {
                      dc.sendSignal("normal_horizontal");
                      directionHolder[0] = Direction.LEFT;
                    }
                  }

                  if (currentState.contains("vertical")) {
                    if (currentState.startsWith("blue")) {
                      dc.sendSignal("reverse_vertical");
                      directionHolder[0] = Direction.DOWN;
                    } else {
                      dc.sendSignal("normal_vertical");
                      directionHolder[0] = Direction.UP;
                    }
                  }
                });
      } else if (tractorBeamEntity.name().equals("beamEmitter")) {
        final Direction dir = directionHolder[0];
        tractorBeamEntity
            .fetch(CollideComponent.class)
            .ifPresent(
                cc -> {
                  cc.onHold(
                      (you, other, collisionDir) -> {
                        other
                            .fetch(VelocityComponent.class)
                            .ifPresent(
                                vc -> {
                                  if (!other.isPresent(FlyComponent.class)) {
                                    other.add(new FlyComponent());
                                  }
                                  Vector2 forceVector =
                                      Vector2.of(
                                          -dir.x() * forceMagnitude, -dir.y() * forceMagnitude);
                                  vc.applyForce("beamEmitter", forceVector);
                                });
                      });
                });
      }
    }
  }

  /**
   * Extends an existing tractor beam by creating additional beam entities in the given direction
   * starting from the specified point.
   *
   * <p>New tractor beam entities are added to both the provided list and the game world. The newly
   * created beam emitter has its DrawComponent removed to avoid duplicate rendering.
   *
   * @param direction the direction in which to extend the beam
   * @param from the starting point of the extended beam segment
   * @param tractorBeamEntities the list of existing tractor beam entities to append to
   * @param extendComp the component so the new entity has the same components as its original
   * @param tbc the component so the new entity has the same components as its original
   */
  public static void extendTractorBeam(
      Direction direction,
      Point from,
      List<Entity> tractorBeamEntities,
      PortalExtendComponent extendComp,
      TractorBeamComponent tbc) {
    TractorBeamFactory factory = new TractorBeamFactory(from, direction);

    while (factory.hasNext()) {
      Entity beamEntity = factory.createNextEntity();
      tractorBeamEntities.add(beamEntity);
      Game.add(beamEntity);
    }

    Entity emitter = factory.createBeamEmitter(from, direction);
    emitter.add(tbc);
    emitter.add(extendComp);
    emitter.remove(DrawComponent.class);
    tractorBeamEntities.add(emitter);
    Game.add(emitter);
  }

  /**
   * Removes all entities from the given list that come after the first `beamEmitter` entity.
   *
   * <p>The first `beamEmitter` and all entities before it remain in the list.
   *
   * @param entities the list of entities to trim.
   */
  public static void trimAfterFirstBeamEmitter(List<Entity> entities) {
    int firstEmitterIndex = -1;

    for (int i = 0; i < entities.size(); i++) {
      if ("beamEmitter".equals(entities.get(i).name())) {
        firstEmitterIndex = i;
        break;
      }
    }

    // remove all entities after first emitter
    if (firstEmitterIndex != -1 && firstEmitterIndex + 1 < entities.size()) {
      List<Entity> toRemove =
          new ArrayList<>(entities.subList(firstEmitterIndex + 1, entities.size()));
      for (Entity entity : toRemove) {
        Game.remove(entity);
      }
      entities.subList(firstEmitterIndex + 1, entities.size()).clear();
    }
  }
}
