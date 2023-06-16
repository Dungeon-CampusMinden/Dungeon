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
        getEntityStream()
                // Consider only entities that have a ProjectileComponent
                .map(this::buildDataObject)
                .map(this::setVelocity)
                // Filter all entities that have reached their endpoint
                .filter(
                        psd ->
                                hasReachedEndpoint(
                                        psd.prc.getStartPosition(),
                                        psd.prc.getGoalLocation(),
                                        psd.pc.position()))
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
        data.vc.setCurrentYVelocity(data.vc.getYVelocity());
        data.vc.setCurrentXVelocity(data.vc.getXVelocity());

        return data;
    }

    private void removeEntitiesOnEndpoint(PSData data) {
        Game.removeEntity(data.pc.getEntity());
    }

    /**
     * Check if the projectile has reached its endpoint or is out of range.
     *
     * <p>A Projectile can be out of range, if it "skips" the endpoint, it has already reached the
     * endpoint and can be removed.
     *
     * @param start position to start the calculation
     * @param end point to check if projectile has reached its goal
     * @param current current position
     * @return true if the endpoint was reached or passed, else false
     */
    private boolean hasReachedEndpoint(Point start, Point end, Point current) {
        float dx = start.x - current.x;
        float dy = start.y - current.y;
        double distanceToStart = Math.sqrt(dx * dx + dy * dy);

        dx = start.x - end.x;
        dy = start.y - end.y;
        double totalDistance = Math.sqrt(dx * dx + dy * dy);

        return distanceToStart > totalDistance;
    }

    // private record to hold all data during streaming
    private record PSData(
            Entity e, ProjectileComponent prc, PositionComponent pc, VelocityComponent vc) {}
}
