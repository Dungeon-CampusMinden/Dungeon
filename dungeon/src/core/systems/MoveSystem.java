package core.systems;

import contrib.components.CollideComponent;
import contrib.systems.CollisionSystem;
import contrib.systems.PositionSync;
import contrib.utils.components.collide.CollisionUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

/**
 * System responsible for updating the position of entities based on their velocity, while
 * considering collision and accessibility of the target tiles.
 *
 * <p>The system processes all entities that have both {@link VelocityComponent} and {@link
 * PositionComponent}. It limits velocity to a maximum speed, applies movement, and handles
 * collisions with non-accessible tiles, including special handling for pit tiles if the entity is
 * allowed to enter them.
 */
public class MoveSystem extends System {

  /**
   * Constructs a MoveSystem that requires entities to have {@link VelocityComponent} and {@link
   * PositionComponent}.
   */
  public MoveSystem() {
    super(VelocityComponent.class, PositionComponent.class);
  }

  /**
   * Executes the system logic for all filtered entities.
   *
   * <p>For each entity, calculates the new position based on the current velocity, caps the
   * velocity to the maxSpeed, and updates the position if the target tile is accessible. If the
   * target tile is blocked, tries moving along individual axes to allow sliding along walls and
   * calls the onWallHit callback.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .map(MSData::of)
        .peek(this::updatePosition)
        .map(MSData::e)
        .forEach(PositionSync::syncPosition);
  }

  /**
   * Updates the position of a single entity based on its velocity and collision rules.
   *
   * <p>The velocity is capped at the entity's maxSpeed to prevent moving too fast diagonally. The
   * method checks the accessibility of all corners of the entity's collision box and handles
   * sliding along walls by checking individual axis movement.
   *
   * <p>If the tile is blocked, the entity's {@code onWallHit} callback is invoked.
   *
   * @param data a record containing the entity and its required components
   */
  private void updatePosition(MSData data) {
    VelocityComponent vc = data.vc;

    Vector2 velocity = data.vc.currentVelocity();

    // Cap velocity magnitude to maxSpeed, mainly for diagonal movement
    if (velocity.length() > data.vc.maxSpeed()) {
      velocity = velocity.normalize().scale(data.vc.maxSpeed());
    }

    // Calculate scaled velocity vector per frame time
    Vector2 sv = velocity.scale(1f / Game.frameRate());
    Point oldPos = data.pc.position();

    boolean hasCollider = data.cc != null;
    boolean hasHitWall = false;

    // First: move only in X direction
    Point newPos = oldPos.translate(sv.x(), 0);
    if (isCollidingWithLevel(data.cc, newPos, vc)) {
      float wallX = fromWall(newPos.x(), sv.x() > 0);
      if (hasCollider) {
        float xOffset = data.cc.collider().offset().x();
        wallX += sv.x() > 0 ? xOffset : -xOffset;
      }
      newPos = new Point(wallX, newPos.y());
      hasHitWall = true;
    }

    // Then: move in Y direction
    newPos = newPos.translate(0, sv.y());
    if (isCollidingWithLevel(data.cc, newPos, vc)) {
      float wallY = fromWall(newPos.y(), sv.y() > 0);
      if (hasCollider) {
        float yOffset = data.cc.collider().offset().y();
        wallY += sv.y() > 0 ? yOffset : -yOffset;
      }
      newPos = new Point(newPos.x(), wallY);
      hasHitWall = true;
    }

    // Final check if newPos is accessible. If no, abort to oldPos.
    if (hasHitWall && isCollidingWithLevel(data.cc, newPos, vc)) {
      newPos = oldPos;
    }
    data.pc.position(newPos);

    if (hasHitWall) {
      data.vc.onWallHit().accept(data.e);
    }
  }

  /**
   * Returns either the lower or upper edge position of a wall. Adds a small epsilon to avoid
   * floating point precision issues.
   *
   * @param position the current position
   * @param lower whether to return the lower edge (true) or upper edge (false)
   * @return the wall edge position
   */
  private float fromWall(float position, boolean lower) {
    if (lower) {
      return (float) Math.floor(position)
          - CollisionSystem.COLLIDE_SET_DISTANCE; // Lower edge + a bit of distance in -x direction
    } else {
      return (float) Math.ceil(position)
          + CollisionSystem.COLLIDE_SET_DISTANCE; // Upper edge + a bit of distance in +x direction
    }
  }

  private boolean isCollidingWithLevel(CollideComponent cc, Point position, VelocityComponent vc) {
    if (cc == null) {
      return CollisionUtils.isCollidingWithLevel(position, vc);
    }
    return CollisionUtils.isCollidingWithLevel(cc.collider(), position, vc);
  }

  /**
   * Data record bundling entity and its components for processing in this system.
   *
   * @param e the entity
   * @param vc the velocity component
   * @param pc the position component
   * @param cc the collide component (nullable)
   */
  private record MSData(Entity e, VelocityComponent vc, PositionComponent pc, CollideComponent cc) {

    /**
     * Builds an MSData object from the given entity by fetching its required components.
     *
     * @param e the entity
     * @return the constructed MSData object
     */
    public static MSData of(Entity e) {
      VelocityComponent vc =
          e.fetch(VelocityComponent.class)
              .orElseThrow(() -> MissingComponentException.build(e, VelocityComponent.class));

      PositionComponent pc =
          e.fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

      CollideComponent cc = e.fetch(CollideComponent.class).orElse(null);

      return new MSData(e, vc, pc, cc);
    }
  }
}
