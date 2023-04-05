package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.ProjectileComponent;
import ecs.entities.Entity;
import graphic.Animation;
import starter.Game;
import tools.Point;

/**
 * The VelocitySystem class is a system responsible for updating the position of entities based on
 * their velocity. It processes entities that have a VelocityComponent and a PositionComponent, and
 * updates their position based on their current velocity. The VelocitySystem also updates the
 * animation of the entity based on its current velocity.
 */
public class VelocitySystem extends ECS_System {

    /**
     * Helper record to conveniently store and access entity, velocity component, and position
     * component together.
     */
    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc) {}

    /**
     * Updates the position of all entities based on their velocity. This method iterates over all
     * entities in the game, and updates their position based on their current velocity. Any
     * projectile entities that hit non-accessible tiles are removed from the game.
     */
    public void update() {
        Game.getEntities().stream()
                .flatMap(e -> e.getComponent(VelocityComponent.class).stream())
                .map(vc -> buildDataObject((VelocityComponent) vc))
                .forEach(this::updatePosition);
    }

    /**
     * Updates the position of an entity based on its current velocity. This method calculates the
     * new position of the entity based on its current position and velocity, and checks if the new
     * position is on an accessible tile on the current level. If the new position is on an
     * accessible tile, the position component of the entity is updated, and the movement animation
     * is updated using the updateMovementAnimation helper method. If the entity is a projectile and
     * its new position is on a non-accessible tile, it is removed from the game. The updated VSData
     * object is returned.
     *
     * @param vsd The VSData object containing entity, velocity, and position components.
     * @return The updated VSData object.
     */
    private VSData updatePosition(VSData vsd) {
        float newX = vsd.pc.getPosition().x + vsd.vc.getCurrentXVelocity();
        float newY = vsd.pc.getPosition().y + vsd.vc.getCurrentYVelocity();
        Point newPosition = new Point(newX, newY);
        if (Game.currentLevel.getTileAt(newPosition.toCoordinate()).isAccessible()) {
            vsd.pc.setPosition(newPosition);
            updateMovmentAnimation(vsd.e);
        }

        // remove projectiles that hit the wall or other non-accessible
        // tiles
        else if (vsd.e.getComponent(ProjectileComponent.class).isPresent())
            Game.removeEntity(vsd.e);

        vsd.vc.setCurrentYVelocity(0);
        vsd.vc.setCurrentXVelocity(0);

        return vsd;
    }

    /**
     * Builds a VSData object using the provided VelocityComponent. The VSData object holds
     * references to the Entity, VelocityComponent, and PositionComponent components for easier
     * access to the components during system updates.
     *
     * @param vc the VelocityComponent of the entity
     * @return the VSData object containing the Entity, VelocityComponent, and PositionComponent
     *     components
     */
    private VSData buildDataObject(VelocityComponent vc) {
        Entity e = vc.getEntity();

        PositionComponent pc =
                (PositionComponent)
                        e.getComponent(PositionComponent.class)
                                .orElseThrow(VelocitySystem::missingPC);

        return new VSData(e, vc, pc);
    }

    /**
     * Updates the movement animation of an entity based on its current velocity.
     *
     * @param entity the entity to update the movement animation for
     */
    private void updateMovmentAnimation(Entity entity) {
        AnimationComponent ac =
                (AnimationComponent)
                        entity.getComponent(AnimationComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("AnimationComponent"));
        Animation newCurrentAnimation;
        VelocityComponent vc =
                (VelocityComponent)
                        entity.getComponent(VelocityComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("VelocityComponent"));
        float x = vc.getCurrentXVelocity();
        if (x > 0) newCurrentAnimation = vc.getMoveRightAnimation();
        else if (x < 0) newCurrentAnimation = vc.getMoveLeftAnimation();
        // idle
        else {
            if (ac.getCurrentAnimation() == ac.getIdleLeft()
                    || ac.getCurrentAnimation() == vc.getMoveLeftAnimation())
                newCurrentAnimation = ac.getIdleLeft();
            else newCurrentAnimation = ac.getIdleRight();
        }
        ac.setCurrentAnimation(newCurrentAnimation);
    }

    private static MissingComponentException missingPC() {
        return new MissingComponentException("PositionComponent");
    }
}
