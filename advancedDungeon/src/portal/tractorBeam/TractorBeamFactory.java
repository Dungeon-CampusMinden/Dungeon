package portal.tractorBeam;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.GlasswandTile;
import core.level.elements.tile.PortalTile;
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
import java.util.*;
import portal.portals.abstraction.Calculations;
import portal.portals.components.PortalComponent;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;
import starter.PortalStarter;

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
 * <p>Reversing the beam changes its color from blue to red.
 */
public class TractorBeamFactory {

  private static final SimpleIPath TRACTOR_BEAM = new SimpleIPath("portal/tractor_beam");
  private static final SimpleIPath BEAM_EMITTER = new SimpleIPath("portal/beam_emitter");
  private static final SimpleIPath PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyCalculations.java");
  private static final String CLASSNAME = "portal.riddles.MyCalculations";
  private final Point from;
  private final Point to;
  private final int totalPoints;
  private int currentIndex = 0;
  private final Direction beamDirection;

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
   * Reverses the direction of the given tractor beam.
   *
   * <p>This method resolves the {@link TractorBeamComponent} from the provided tractor beam entity
   * and applies a reverse operation to all underlying tractor beam entities managed by that
   * component.
   *
   * <p>The caller is expected to ensure that the given entity represents a valid tractor beam and
   * contains a {@link TractorBeamComponent}.
   *
   * @param tractorBeam the tractor beam entity whose direction or force should be reversed
   */
  public static void reverse(Entity tractorBeam) {
    reverseTractorBeam(
        tractorBeam.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
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
   * Determines the last available point for a tractor beam. The beam is stopped by a wallTile, so
   * the EndPoint has to be last point in front of a wall tile.
   *
   * <p>Allow PortalTile on the first and second iteration (start or immediate portal exit).
   *
   * @param from the starting point
   * @param beamDirection the emitted direction of the tractor beam
   * @return the last available point
   */
  private Point calculateEndPoint(Point from, Direction beamDirection) {
    Point lastPoint = from;
    Tile lastTile = Game.tileAt(lastPoint).orElse(null);
    Point currentPoint = from;
    Tile currentTile = Game.tileAt(from).orElse(null);
    boolean firstStep = true;
    boolean secondStep = false;

    while (currentTile != null
        && !currentTile.getClass().equals(WallTile.class)
        && !currentTile.getClass().equals(GlasswandTile.class)
        && (firstStep || secondStep || !lastTile.getClass().equals(PortalTile.class))) {
      secondStep = firstStep;
      firstStep = false;
      lastPoint = currentPoint;
      lastTile = Game.tileAt(lastPoint).orElse(null);
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
    tractorBeam
        .fetch(PositionComponent.class)
        .ifPresent(pc -> pc.rotation(rotationFor(beamDirection)));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(TRACTOR_BEAM);

    State blue = State.fromMap(animationMap, "blue");
    State red = State.fromMap(animationMap, "red");
    StateMachine sm = new StateMachine(Arrays.asList(blue, red));
    sm.addTransition(blue, "reverse_color", red);
    sm.addTransition(red, "normalize_color", blue);
    DrawComponent dc = new DrawComponent(sm, DepthLayer.Ground);
    tractorBeam.add(dc);

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
    pec.onExtend = tbc::extend;
    pec.onTrim = (e) -> tbc.trim();
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
    beamEmitter.add(new PortalIgnoreComponent());
    beamEmitter.add(new PositionComponent(spawnPoint));
    beamEmitter.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    beamEmitter.fetch(PositionComponent.class).ifPresent(pc -> pc.viewDirection(direction));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(BEAM_EMITTER);
    StateMachine sm;
    float hitboxX = 1f;
    float hitboxY = 1f;
    float offsetX = 0f;
    float offsetY = 0f;

    State idle = State.fromMap(animationMap, "idle");
    sm = new StateMachine(List.of(idle));
    beamEmitter.add(new DrawComponent(sm));

    switch (direction) {
      case Direction.LEFT:
        hitboxX = currentIndex;
        offsetX = (-currentIndex) + 1;
        break;
      case Direction.UP:
        hitboxY = currentIndex;
        break;
      case Direction.RIGHT:
        hitboxX = currentIndex;
        break;
      case Direction.DOWN:
        hitboxY = currentIndex;
        offsetY = (-currentIndex) + 1;
        break;
    }

    TriConsumer<Entity, Entity, Direction> action =
        (you, other, collisionDir) ->
            other
                .fetch(VelocityComponent.class)
                .ifPresent(
                    vc -> {
                      if (!you.fetch(TractorBeamComponent.class).get().isActive()) {
                        return;
                      }
                      if (other.isPresent(PortalComponent.class)) {
                        return;
                      }
                      if (!other.isPresent(FlyComponent.class)) {
                        other.add(new FlyComponent());
                      }
                      Vector2 forceVector = beamForce();

                      if (you.fetch(TractorBeamComponent.class)
                          .get()
                          .oldForces
                          .containsKey(other)) {
                        Vector2 oldForce =
                            you.fetch(TractorBeamComponent.class).get().oldForces.get(other);
                        if (oldForce.x() == 0 && forceVector.x() == 0) {
                          vc.applyForce("beamEmitter", forceVector);
                        } else if (oldForce.y() == 0 && forceVector.y() == 0) {
                          vc.applyForce("beamEmitter", forceVector);
                        }
                      } else {
                        vc.applyForce("beamEmitter", forceVector);
                        you.fetch(TractorBeamComponent.class)
                            .get()
                            .oldForces
                            .put(other, forceVector);
                      }
                    });

    TriConsumer<Entity, Entity, Direction> actionLeave =
        (you, other, collisionDir) -> {
          you.fetch(TractorBeamComponent.class).get().oldForces.remove(other);
          if (other.fetch(ProjectileComponent.class).isEmpty()) {
            other.remove(FlyComponent.class);
          }
          if (other.isPresent(VelocityComponent.class)) {
            other.fetch(VelocityComponent.class).get().currentVelocity(Vector2.ZERO);
            other.fetch(VelocityComponent.class).get().clearForces();
          }
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

  private Vector2 beamForce() {
    Object o;
    try {
      o = DynamicCompiler.loadUserInstance(PATH, CLASSNAME);
      return ((Calculations) o).beamForce(beamDirection);
    } catch (Exception e) {
      if (PortalStarter.DEBUG_MODE) e.printStackTrace();
      DialogUtils.showTextPopup("TBD", "Code Error");
    }
    return Vector2.ZERO;
  }

  /**
   * Reverses a tractor beam. Changes the force direction and color.
   *
   * @param tractorBeamEntities The list of all entities building the tractor beam
   */
  public static void reverseTractorBeam(List<Entity> tractorBeamEntities) {
    // don't reverse the beam if it is deactivated
    if (!checkActiveState(tractorBeamEntities)) {
      return;
    }

    Entity lastEntity = null;
    for (Entity tractorBeamEntity : tractorBeamEntities) {
      lastEntity = tractorBeamEntity;
      if (tractorBeamEntity.name().equals("tractorBeam")) {
        tractorBeamEntity
            .fetch(DrawComponent.class)
            .ifPresent(
                dc -> {
                  String currentState = dc.currentStateName();

                  if (currentState.contains("blue")) {
                    dc.sendSignal("reverse_color");
                  } else if (currentState.contains("red")) {
                    dc.sendSignal("normalize_color");
                  }
                });

      } else if (tractorBeamEntity.name().equals("beamEmitter")) {
        if (tractorBeamEntity.fetch(PositionComponent.class).isEmpty()) {
          return;
        }
        if (tractorBeamEntity.fetch(TractorBeamComponent.class).isEmpty()) {
          return;
        }
        Direction viewDir = tractorBeamEntity.fetch(PositionComponent.class).get().viewDirection();
        boolean isReversed = tractorBeamEntity.fetch(TractorBeamComponent.class).get().isReversed();

        if (isReversed) {
          viewDir = viewDir.opposite();
        }
        final Direction dir = viewDir;

        tractorBeamEntity
            .fetch(CollideComponent.class)
            .ifPresent(
                cc ->
                    cc.onHold(
                        (you, other, collisionDir) ->
                            other
                                .fetch(VelocityComponent.class)
                                .ifPresent(
                                    vc -> {
                                      if (!other.isPresent(FlyComponent.class)) {
                                        other.add(new FlyComponent());
                                      }
                                      Vector2 forceVector = reversedBeamForce(dir);
                                      vc.applyForce("beamEmitter", forceVector);
                                    })));
      }
    }
    if (lastEntity != null) {
      lastEntity.fetch(TractorBeamComponent.class).ifPresent(TractorBeamComponent::toggleReversed);
    }
  }

  private static Vector2 reversedBeamForce(Direction dir) {
    Object o;
    try {
      o = DynamicCompiler.loadUserInstance(PATH, CLASSNAME);
      return ((Calculations) o).reversedBeamForce(dir);
    } catch (Exception e) {
      if (PortalStarter.DEBUG_MODE) e.printStackTrace();
      DialogUtils.showTextPopup("TBD", "Code Error");
    }
    return Vector2.ZERO;
  }

  /**
   * Reverses all tractor beam segments after the first beamEmitter. This ensures that the extended
   * beam has the correct texture orientation and applies force in the correct direction.
   *
   * @param extensionBeamEntities the list of all entities that form the extended tractor beam
   */
  public static void reverseExtensionBeam(List<Entity> extensionBeamEntities) {
    // find index of first beamEmitter
    int firstEmitterIndex = -1;
    for (int i = 0; i < extensionBeamEntities.size(); i++) {
      if (extensionBeamEntities.get(i).name().equals("beamEmitter")) {
        firstEmitterIndex = i;
        break;
      }
    }

    if (firstEmitterIndex == -1) return;

    // reverse all entities after the first beamEmitter
    int i = firstEmitterIndex + 1;

    while (i < extensionBeamEntities.size()) {

      while (i < extensionBeamEntities.size()
          && extensionBeamEntities.get(i).name().equals("tractorBeam")) {

        Entity tractorBeamEntity = extensionBeamEntities.get(i);

        tractorBeamEntity
            .fetch(DrawComponent.class)
            .ifPresent(
                dc -> {
                  String currentState = dc.currentStateName();

                  if (currentState.contains("blue")) {
                    dc.sendSignal("reverse_color");
                  } else if (currentState.contains("red")) {
                    dc.sendSignal("normalize_color");
                  }
                });

        i++;
      }

      if (i < extensionBeamEntities.size()
          && extensionBeamEntities.get(i).name().equals("beamEmitter")) {

        Entity emitter = extensionBeamEntities.get(i);
        if (emitter.fetch(PositionComponent.class).isEmpty()) {
          return;
        }
        final Direction dir = emitter.fetch(PositionComponent.class).get().viewDirection();

        emitter
            .fetch(CollideComponent.class)
            .ifPresent(
                cc ->
                    cc.onHold(
                        (you, other, collisionDir) ->
                            other
                                .fetch(VelocityComponent.class)
                                .ifPresent(
                                    vc -> {
                                      if (!other.isPresent(FlyComponent.class)) {
                                        other.add(new FlyComponent());
                                      }
                                      Vector2 forceVector = reversedBeamForce(dir);
                                      vc.applyForce("beamEmitter", forceVector);
                                    })));

        i++;
      } else {
        break;
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

    if (tbc.isReversed()) {
      reverseExtensionBeam(tractorBeamEntities);
    }
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

  /**
   * Checks the active status of a tractor beam by fetching it from the TractorBeamComponent.
   *
   * @param tractorBeamEntities The list of all entities building the tractor beam
   * @return the current status of the "active" status
   */
  public static boolean checkActiveState(List<Entity> tractorBeamEntities) {
    for (Entity entity : tractorBeamEntities) {
      if (entity.name().equals("beamEmitter")) {
        TractorBeamComponent tbc = entity.fetch(TractorBeamComponent.class).orElse(null);
        if (tbc != null) {
          return tbc.isActive();
        }
      }
    }
    return false;
  }

  /**
   * Returns the rotation in degrees for a given direction. 0° corresponds to RIGHT, 180° to LEFT,
   * 90° to UP, and -90° to DOWN.
   *
   * @param d the direction
   * @return the rotation in degrees for rendering
   */
  private static float rotationFor(Direction d) {
    return switch (d) {
      case UP -> 90f;
      case DOWN -> -90f;
      case LEFT -> 180f;
      case RIGHT -> 0f;
      default -> 0f;
    };
  }
}
