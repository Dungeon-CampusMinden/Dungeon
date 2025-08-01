package core.systems;

import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.draw.CoreAnimations;
import java.util.logging.Logger;

/**
 * The VelocitySystem manages the movement and animation state of entities based on their velocity.
 *
 * <p>Entities with {@link VelocityComponent}, {@link PositionComponent}, and {@link DrawComponent}
 * are processed by this system. It calculates the new velocity by applying forces and mass, updates
 * the current velocity, and controls the animation state (run or idle) depending on movement.
 *
 * <p>The {@link MoveSystem} will perform the movement.
 *
 * <p>If an entity is moving, the corresponding run animation is queued and the view direction
 * updated. If the entity is idle, the idle animation matching the view direction is queued.
 *
 * <p>At the end of processing, applied forces are cleared and velocity is updated accordingly.
 *
 * @see VelocityComponent
 */
public final class VelocitySystem extends System {

  private static final Logger LOGGER = Logger.getLogger(VelocitySystem.class.getName());

  // Default time (frames) an animation should be enqueued for
  private static final int DEFAULT_FRAME_TIME = 1;

  /** Constructs a new VelocitySystem. */
  public VelocitySystem() {
    super(VelocityComponent.class, PositionComponent.class, DrawComponent.class);
  }

  /**
   * Executes the system logic for all filtered entities.
   *
   * <p>For each entity, calculates velocity based on applied forces and mass, updates current
   * velocity, clears forces, and queues appropriate animations.
   */
  @Override
  public void execute() {
    filteredEntityStream(VelocityComponent.class, PositionComponent.class, DrawComponent.class)
        .map(this::buildDataObject)
        .map(this::calculateVelocity)
        .forEach(this::movementAnimation);
  }

  /**
   * Calculates the new velocity of an entity by summing applied forces and dividing by mass.
   *
   * @param vsd containing entity and components
   * @return updated VSData with modified velocity
   */
  private VSData calculateVelocity(VSData vsd) {
    Vector2 sumForces = vsd.vc.appliedForcesStream().reduce(Vector2.of(0, 0), Vector2::add);

    float mass = vsd.vc().mass();
    // acceleration = force / mass
    Vector2 acceleration = sumForces.scale(1.0 / mass);

    Vector2 newVelocity = vsd.vc.currentVelocity().add(acceleration);
    if (newVelocity.isZero()) newVelocity = Vector2.ZERO;

    vsd.vc.currentVelocity(newVelocity);
    vsd.vc.clearForces();

    return vsd;
  }

  /**
   * Queues the appropriate animation based on the entity's velocity and updates its view direction.
   *
   * <p>If the entity is moving, enqueues run animations depending on the dominant velocity axis. If
   * idle, enqueues idle animation matching the current view direction.
   *
   * @param vsd containing entity and components
   */
  private void movementAnimation(VSData vsd) {
    float x = vsd.vc.currentVelocity().x();
    float y = vsd.vc.currentVelocity().y();

    // Entity is moving
    if (x != 0 || y != 0) {
      // Remove any queued run or idle animations to avoid conflicts
      vsd.dc.deQueueByPriority(CoreAnimationPriorities.RUN.priority());
      vsd.dc.deQueueByPriority(CoreAnimationPriorities.IDLE.priority());

      // Use the velocity axis with the greatest magnitude for animation direction
      if (Math.abs(x) >= Math.abs(y)) {
        if (x > 0) {
          vsd.dc.queueAnimation(CoreAnimations.RUN_RIGHT, CoreAnimations.RUN);
          vsd.pc.viewDirection(Direction.RIGHT);
        } else {
          vsd.dc.queueAnimation(CoreAnimations.RUN_LEFT, CoreAnimations.RUN);
          vsd.pc.viewDirection(Direction.LEFT);
        }
      } else if (y > 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_UP, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.UP);
      } else {
        vsd.dc.queueAnimation(CoreAnimations.RUN_DOWN, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.DOWN);
      }
    }
    // Entity is idle
    else {
      // Queue idle animation based on current view direction
      switch (vsd.pc.viewDirection()) {
        case UP ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_UP,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE_RIGHT);
        case LEFT ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_RIGHT,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE_UP);
        case DOWN ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_UP,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE_RIGHT);
        case RIGHT ->
            vsd.dc.queueAnimation(
                DEFAULT_FRAME_TIME,
                CoreAnimations.IDLE_RIGHT,
                CoreAnimations.IDLE,
                CoreAnimations.IDLE_LEFT,
                CoreAnimations.IDLE_DOWN,
                CoreAnimations.IDLE_UP);
        case NONE -> // Invalid direction, log warning
            LOGGER.warning(
                "Entity "
                    + vsd.e.id()
                    + " has an invalid view direction: "
                    + vsd.pc.viewDirection()
                    + ". Cannot queue idle animation.");
      }
    }
  }

  /**
   * Builds a data record with the entity and its required components. Throws {@link
   * MissingComponentException} if components are missing.
   *
   * @param e the entity to process
   * @return a VSData record bundling entity and components
   */
  private VSData buildDataObject(Entity e) {
    VelocityComponent vc =
        e.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, VelocityComponent.class));

    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    DrawComponent dc =
        e.fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, DrawComponent.class));

    return new VSData(e, vc, pc, dc);
  }

  /**
   * Record bundling entity and its components for processing in VelocitySystem.
   *
   * @param e the entity
   * @param vc the velocity component
   * @param pc the position component
   * @param dc the draw component
   */
  private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}
}
