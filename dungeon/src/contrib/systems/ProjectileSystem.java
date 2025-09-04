package contrib.systems;

import contrib.components.ProjectileComponent;
import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;

/**
 * The ProjectileSystem class represents a system responsible for managing {@link
 * ProjectileComponent}s in the game. It checks if projectiles have reached their endpoints and
 * executes the {@link ProjectileComponent#onEndReached()} callback.
 *
 * <p>Note that the velocity of the projectile is not managed in this system. That is done by the
 * {@link core.systems.VelocitySystem}.
 *
 * <p>Entities with the {@link ProjectileComponent}, a {@link PositionComponent} and {@link
 * VelocityComponent} will be processed by this system.
 */
public final class ProjectileSystem extends System {

  private final String PROJECTILE_FORCE = "Projectile";

  /** Create a new ProjectileSystem. */
  public ProjectileSystem() {
    super(ProjectileComponent.class, PositionComponent.class, VelocityComponent.class);
  }

  /** Sets the velocity and removes entities that have reached their endpoints. */
  @Override
  public void execute() {
    filteredEntityStream(
            ProjectileComponent.class, PositionComponent.class, VelocityComponent.class)
        // Consider only entities that have a ProjectileComponent
        .map(this::buildDataObject)
        .map(this::applyForce)
        // Filter all entities that have reached their endpoint
        .filter(this::hasReachedEndpoint)
        // Trigger endpoint-callback for all entities who reached their endpoint
        .forEach(this::executeEntitiesOnEndpoint);
  }

  private PSData buildDataObject(final Entity entity) {

    ProjectileComponent prc =
        entity
            .fetch(ProjectileComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, ProjectileComponent.class));
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));
    return new PSData(entity, prc, pc, vc);
  }

  private PSData applyForce(final PSData data) {
    if (data.vc().currentVelocity().isZero())
      data.vc.applyForce(PROJECTILE_FORCE, data.prc.forceToApply());
    return data;
  }

  private void executeEntitiesOnEndpoint(final PSData data) {
    data.prc().onEndReached().accept(data.e());
  }

  /**
   * Check if the projectile has reached its endpoint or is out of range.
   *
   * <p>A Projectile can be out of range if it "skips" the endpoint. It has already reached the
   * endpoint and can be removed.
   *
   * @param psd The PSData to check if the projectile has reached the endpoint.
   * @return true if the endpoint was reached or passed, else false.
   */
  private boolean hasReachedEndpoint(final PSData psd) {
    Point start = psd.prc.startPosition();
    Point end = psd.prc.goalLocation();
    Point current = psd.pc.position();

    double distanceToStart = Point.calculateDistance(start, current);

    double totalDistance = Point.calculateDistance(start, end);

    return distanceToStart > totalDistance;
  }

  // private record to hold all data during streaming
  private record PSData(
      Entity e, ProjectileComponent prc, PositionComponent pc, VelocityComponent vc) {}
}
