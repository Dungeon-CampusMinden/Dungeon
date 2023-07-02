package contrib.systems;

import contrib.components.ProjectileComponent;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;

/**
 * The ProjectileSystem class represents a system responsible for managing {@link
 * ProjectileComponent}s in the game. It checks if projectiles have reached their endpoints and
 * removes entities that have reached their endpoints.
 *
 * <p>Note that the velocity of the projectile is not managed in this system, that is done by the
 * {@link core.systems.VelocitySystem}.
 *
 * <p>The components required for this system are {@link ProjectileComponent}, {@link
 * PositionComponent}, and {@link VelocityComponent}.
 */
public class ProjectileSystem extends System {

    public ProjectileSystem() {
        super(ProjectileComponent.class, PositionComponent.class, VelocityComponent.class);
    }

    /** Sets the velocity and removes entities that have reached their endpoints. */
    @Override
    public void execute() {
        entityStream()
                // Consider only entities that have a ProjectileComponent
                .map(this::buildDataObject)
                .map(this::setVelocity)
                // Filter all entities that have reached their endpoint
                .filter(this::hasReachedEndpoint)
                // Remove all entities who reached their endpoint
                .forEach(this::removeEntitiesOnEndpoint);
    }

    private PSData buildDataObject(Entity entity) {

        ProjectileComponent prc =
                entity.fetch(ProjectileComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, ProjectileComponent.class));
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        VelocityComponent vc =
                entity.fetch(VelocityComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, VelocityComponent.class));
        return new PSData(entity, prc, pc, vc);
    }

    private PSData setVelocity(PSData data) {
        data.vc.currentYVelocity(data.vc.yVelocity());
        data.vc.currentXVelocity(data.vc.xVelocity());

        return data;
    }

    private void removeEntitiesOnEndpoint(PSData data) {
        Game.removeEntity(data.pc.entity());
    }

    /**
     * Check if the projectile has reached its endpoint or is out of range.
     *
     * <p>A Projectile can be out of range, if it "skips" the endpoint, it has already reached the
     * endpoint and can be removed.
     *
     * @param psd the PSData to check if the projectile has reached the endpoint
     * @return true if the endpoint was reached or passed, else false
     */
    private boolean hasReachedEndpoint(PSData psd) {
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
