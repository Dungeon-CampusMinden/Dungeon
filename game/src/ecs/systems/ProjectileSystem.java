package ecs.systems;

import ecs.components.*;
import ecs.components.skill.ProjectileComponent;
import ecs.entities.Entity;
import starter.Game;
import tools.Point;

public class ProjectileSystem extends ECS_System {

    // private record to hold all data during streaming
    private record HSData(
            Entity e, ProjectileComponent prc, PositionComponent pc, VelocityComponent vc) {}

    /** reduces the cool down for all skills */
    @Override
    public void update() {
        Game.entities.stream()
                // Consider only entities that have a ProjectileComponent
                .flatMap(e -> e.getComponent(ProjectileComponent.class).stream())
                .map(hc -> buildDataObject((ProjectileComponent) hc))
                // Apply damage
                .map(this::setVelocity)
                // Filter all dead entities
                .filter(
                        hsd ->
                                hasReachedEndpoint(
                                        hsd.prc.getStartPosition(),
                                        hsd.prc.getGoalLocation(),
                                        hsd.pc.getPosition()))
                // Remove all dead entities
                .forEach(this::removeDeadEntities);
    }

    private ProjectileSystem.HSData buildDataObject(ProjectileComponent prc) {
        Entity e = prc.getEntity();

        PositionComponent pc =
                (PositionComponent)
                        e.getComponent(PositionComponent.class)
                                .orElseThrow(ProjectileSystem::missingAC);
        VelocityComponent vc =
                (VelocityComponent)
                        e.getComponent(VelocityComponent.class)
                                .orElseThrow(ProjectileSystem::missingAC);

        return new HSData(e, prc, pc, vc);
    }

    private HSData setVelocity(HSData data) {
        data.vc.setCurrentYVelocity(data.vc.getYVelocity());
        data.vc.setCurrentXVelocity(data.vc.getXVelocity());

        return data;
    }

    private void removeDeadEntities(HSData data) {
        Game.entitiesToRemove.add(data.pc.getEntity());
    }

    public boolean hasReachedEndpoint(Point start, Point end, Point current) {
        float dx = start.x - current.x;
        float dy = start.y - current.y;
        double distanceToStart = Math.sqrt(dx * dx + dy * dy);

        dx = start.x - end.x;
        dy = start.y - end.y;
        double totalDistance = Math.sqrt(dx * dx + dy * dy);

        if (distanceToStart > totalDistance) {
            // The point has reached or passed the endpoint
            return true;
        } else {
            // The point has not yet reached the endpoint
            return false;
        }
    }

    private static MissingComponentException missingAC() {
        return new MissingComponentException("AnimationComponent");
    }
}
