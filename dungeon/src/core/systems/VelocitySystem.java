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

/**
 * The VelocitySystem controls the movement of the entities in the game.
 *
 * <p>Entities with the {@link VelocityComponent}, {@link PositionComponent}, and {@link
 * DrawComponent} will be processed by this system.
 *
 * <p>The system will take the {@link VelocityComponent#currentVelocity()} and calculate the new
 * position of the entity based on their current position stored in the {@link PositionComponent}.
 * If the new position is a valid position, which means the tile they would stand on is accessible,
 * the new position will be set. If the new position is walled, the {@link
 * VelocityComponent#onWallHit()} callback will be executed.
 *
 * <p>This system will also queue the corresponding run or idle animation.
 *
 * <p>At the end, the {@link VelocityComponent#currentVelocity(Vector2)} will be set to 0.
 *
 * @see VelocityComponent
 * @see DrawComponent
 * @see PositionComponent
 * @see core.level.elements.ILevel
 */
public final class VelocitySystem extends System {

  // default time an Animation should be enqueued
  private static final int DEFAULT_FRAME_TIME = 1;

  /** Create a new VelocitySystem. */
  public VelocitySystem() {
    super(VelocityComponent.class, PositionComponent.class, DrawComponent.class);
  }

  /** Updates the position of all entities based on their velocity. */
  @Override
  public void execute() {
    filteredEntityStream(VelocityComponent.class, PositionComponent.class, DrawComponent.class)
        .map(this::buildDataObject)
        .map(this::calculateVelocity)
        .forEach(this::movementAnimation);
  }

  private VSData calculateVelocity(VSData vsd) {
    // TODO calculate velocity with FORCE
    vsd.vc.currentVelocity(vsd.vc.currentVelocity());
    return vsd;
  }

  private void movementAnimation(VSData vsd) {
    float x = vsd.vc.currentVelocity().x();
    float y = vsd.vc.currentVelocity().y();

    // move
    if (x != 0 || y != 0) {
      vsd.dc.deQueueByPriority(CoreAnimationPriorities.RUN.priority());
      if (x > 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_RIGHT, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.RIGHT);
      } else if (x < 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_LEFT, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.LEFT);
      } else if (y > 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_UP, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.UP);
      } else if (y < 0) {
        vsd.dc.queueAnimation(CoreAnimations.RUN_DOWN, CoreAnimations.RUN);
        vsd.pc.viewDirection(Direction.DOWN);
      }

      vsd.dc.deQueueByPriority(CoreAnimationPriorities.IDLE.priority());
    }
    // idle
    else {
      // each drawComponent has an idle animation, so no check is needed
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
        case NONE -> // Invalid direction
            LOGGER.warning(
                "Entity "
                    + vsd.e.id()
                    + " has an invalid view direction: "
                    + vsd.pc.viewDirection()
                    + ". Cannot queue idle animation.");
      }
    }
  }

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

  private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}
}
