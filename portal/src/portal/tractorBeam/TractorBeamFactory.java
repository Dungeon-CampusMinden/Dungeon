package portal.tractorBeam;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import portal.PortalRegistry;
import portal.portals.components.PortalExtendComponent;
import portal.portals.components.PortalIgnoreComponent;

/** A factory for creating tractor beam entities between two points. */
public class TractorBeamFactory {

  private static final SimpleIPath TRACTOR_BEAM = new SimpleIPath("portal/tractor_beam");
  private static final SimpleIPath BEAM_EMITTER = new SimpleIPath("portal/beam_emitter");
  private static final String BEAMFORCE =
      "Die Berechnung der Kraft des Traktorstrahls ist nicht richtig.";
  private static String beamEmitterName = "beamEmitter";
  private static String tractorBeamName = "tractorBeam";

  /**
   * Creates a tractor beam. It only needs a spawn point and an emitted direction. The beam is
   * stopped by the next wall.
   *
   * @param from the starting point of the beam
   * @param direction the emitted direction of the tractor beam
   * @return a list of all tractor beam entities
   */
  public static Entity createTractorBeam(Point from, Direction direction) {
    Entity beamEmitter = createBeamEmitter(from, direction);

    TractorBeamComponent tbc = new TractorBeamComponent();
    beamEmitter.add(tbc);

    PortalExtendComponent pec = new PortalExtendComponent();
    pec.onExtend =
        (dir, point, portalExtendComponent) -> {
          Point to = TractorBeamUtil.calculateEndPoint(from, direction);
          int totalPoints = TractorBeamUtil.calculateNumberOfPoints(from, to);

          for (int i = 0; i < totalPoints; i++) {
            Entity beamElement =
                TractorBeamFactory.createNextEntity(
                    from.translate(direction.scale(i + 1)), direction, tbc);
            Game.add(beamElement);
          }

          Entity emitter = TractorBeamFactory.createBeamEmitter(from, direction);
          emitter.add(tbc);
          emitter.remove(DrawComponent.class);
          Game.add(emitter);
        };
    pec.onTrim = (e) -> TractorBeamUtil.trimAfterFirstBeamEmitter(tbc);
    beamEmitter.add(pec);

    return beamEmitter;
  }

  /**
   * Creates the next entity of the tractor beam.
   *
   * <p>This entity is one part of the visual representation for the whole beam.
   *
   * @return a new tractor beam entity, or {@code null} if no more entities can be created
   */
  public static Entity createNextEntity(
      Point point, Direction direction, TractorBeamComponent tractorBeamComponent) {
    Entity tractorBeam = new Entity(tractorBeamName);
    tractorBeam.add(tractorBeamComponent);
    tractorBeam.add(new PositionComponent(point));
    tractorBeam.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(TRACTOR_BEAM);

    State blue = State.fromMap(animationMap, "blue");
    State red = State.fromMap(animationMap, "red");
    StateMachine sm = new StateMachine(Arrays.asList(blue, red));
    sm.addTransition(blue, "reverse_color", red);
    sm.addTransition(red, "normalize_color", blue);
    DrawComponent dc = new DrawComponent(sm, DepthLayer.Ground);
    tractorBeam.add(dc);
    if (tractorBeamComponent.isReversed()) {
      dc.sendSignal("reverse_color");
    }
    return tractorBeam;
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
        TractorBeamUtil.getRelevantEntities(tractorBeam.fetch(TractorBeamComponent.class).get()));
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
  private static Entity createBeamEmitter(Point spawnPoint, Direction direction) {
    Entity beamEmitter = new Entity(beamEmitterName);

    beamEmitter.add(new PortalIgnoreComponent());
    beamEmitter.add(new PositionComponent(spawnPoint));
    beamEmitter.fetch(PositionComponent.class).ifPresent(pc -> pc.rotation(rotationFor(direction)));
    beamEmitter.fetch(PositionComponent.class).ifPresent(pc -> pc.viewDirection(direction));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(BEAM_EMITTER);

    State idle = State.fromMap(animationMap, "idle");
    StateMachine sm;
    sm = new StateMachine(List.of(idle));
    beamEmitter.add(new DrawComponent(sm));

    return beamEmitter;
  }

  private static Vector2 beamForce(Direction dir) {
    Object o;
    try {
      return PortalRegistry.getCalculations().beamForce(dir);
    } catch (Exception e) {
      if (PortalRegistry.isDebugMode()) e.printStackTrace();
      DialogUtils.showTextPopup(BEAMFORCE, "Code Error");
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
      if (tractorBeamEntity.name().equals(tractorBeamName)) {
        handleTractorBeamVisual(tractorBeamEntity);
      } else if (tractorBeamEntity.name().equals(beamEmitterName)) {
        handleBeamEmitter(tractorBeamEntity);
      }
    }
    if (lastEntity != null) {
      lastEntity
          .fetch(TractorBeamComponent.class)
          .ifPresent(
              tbc -> {
                tbc.setReversed(!tbc.isReversed());
              });
    }
  }

  private static void handleBeamEmitter(Entity tractorBeamEntity) {
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
    // dynamically reload calculation of reversed force to apply
    tractorBeamEntity
        .fetch(TractorBeamComponent.class)
        .get()
        .reversedForceToApply(reversedBeamForce(dir));
    tractorBeamEntity.fetch(TractorBeamComponent.class).get().forceToApply(beamForce(dir));
    if (tractorBeamEntity
        .fetch(TractorBeamComponent.class)
        .get()
        .reversedForceToApply()
        .equals(Vector2.ZERO)) {
      return;
    }
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
                                  Vector2 forceVector;
                                  if (tractorBeamEntity
                                      .fetch(TractorBeamComponent.class)
                                      .get()
                                      .isReversed())
                                    forceVector =
                                        tractorBeamEntity
                                            .fetch(TractorBeamComponent.class)
                                            .get()
                                            .reversedForceToApply();
                                  else
                                    forceVector =
                                        tractorBeamEntity
                                            .fetch(TractorBeamComponent.class)
                                            .get()
                                            .forceToApply();
                                  vc.applyForce(beamEmitterName, forceVector);
                                })));
  }

  private static void handleTractorBeamVisual(Entity tractorBeamEntity) {
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
  }

  private static Vector2 reversedBeamForce(Direction dir) {
    Object o;
    try {
      return PortalRegistry.getCalculations().reversedBeamForce(dir);
    } catch (Exception e) {
      if (PortalRegistry.isDebugMode()) e.printStackTrace();
      DialogUtils.showTextPopup(BEAMFORCE, "Code Error");
    }
    return Vector2.ZERO;
  }

  /**
   * Checks the active status of a tractor beam by fetching it from the TractorBeamComponent.
   *
   * @param tractorBeamEntities The list of all entities building the tractor beam
   * @return the current status of the "active" status
   */
  public static boolean checkActiveState(List<Entity> tractorBeamEntities) {
    for (Entity entity : tractorBeamEntities) {
      if (entity.name().equals(beamEmitterName)) {
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
