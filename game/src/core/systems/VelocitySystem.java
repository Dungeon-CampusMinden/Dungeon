package core.systems;

import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The VelocitySystem controls the movement of the entities in the game.
 *
 * <p>Entities with the {@link VelocityComponent}, {@link PositionComponent}, and {@link
 * DrawComponent} will be processed by this system.
 *
 * <p>The system will take the {@link VelocityComponent#getCurrentXVelocity()} and {@link
 * VelocityComponent#getCurrentYVelocity()} and calculate the new position of the entity based on
 * their current position stored in the {@link PositionComponent}. If the new position is a valid
 * position, which means the tile they would stand on is accessible, the new position will be set.
 *
 * <p>This system will also set the current animation to {@link CoreAnimations#RUN_LEFT} or {@link
 * CoreAnimations#RUN_RIGHT} if the position is valid.
 *
 * <p>If the new position is not valid, the {@link CoreAnimations#IDLE_LEFT} or {@link
 * CoreAnimations#IDLE_RIGHT} animations will be set as the new current animation.
 *
 * <p>At the end, the {@link VelocityComponent#setCurrentXVelocity(float)} and {@link
 * VelocityComponent#setYVelocity(float)} will be set to 0.
 *
 * @see VelocityComponent
 * @see DrawComponent
 * @see PositionComponent
 * @see core.level.elements.ILevel
 */
public final class VelocitySystem extends System {

    /** Create a new VelocitySystem */
    public VelocitySystem() {
        super(VelocityComponent.class, PositionComponent.class, DrawComponent.class);
    }

    /** Updates the position of all entities based on their velocity */
    @Override
    public void execute() {
        getEntityStream().map(this::buildDataObject).forEach(this::updatePosition);
    }

    private void updatePosition(VSData vsd) {
        float newX = vsd.pc.position().x + vsd.vc.getCurrentXVelocity();
        float newY = vsd.pc.position().y + vsd.vc.getCurrentYVelocity();
        Point newPosition = new Point(newX, newY);
        if (Game.tileAT(newPosition).isAccessible()) {
            vsd.pc.position(newPosition);
            movementAnimation(vsd);
        }

        // remove projectiles that hit the wall or other non-accessible
        // tiles
        else if (vsd.e.fetch(ProjectileComponent.class).isPresent()) Game.removeEntity(vsd.e);

        vsd.vc.setCurrentYVelocity(0);
        vsd.vc.setCurrentXVelocity(0);
    }

    private VSData buildDataObject(Entity e) {
        VelocityComponent vc =
                e.fetch(VelocityComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(e, VelocityComponent.class));

        PositionComponent pc =
                e.fetch(PositionComponent.class)
                        .orElseThrow(
                                () -> MissingComponentException.build(e, PositionComponent.class));

        DrawComponent dc =
                e.fetch(DrawComponent.class)
                        .orElseThrow(() -> MissingComponentException.build(e, DrawComponent.class));

        return new VSData(e, vc, pc, dc);
    }

    private void movementAnimation(VSData vsd) {

        AtomicBoolean isDead = new AtomicBoolean(false);
        vsd.e
                .fetch(HealthComponent.class)
                .ifPresent(
                        component -> {
                            isDead.set(component.isDead());
                        });

        if (isDead.get()) {
            return;
        }

        float x = vsd.vc.getCurrentXVelocity();
        if (x > 0) vsd.dc.setCurrentAnimation(CoreAnimations.RUN_RIGHT);
        else if (x < 0) vsd.dc.setCurrentAnimation(CoreAnimations.RUN_LEFT);
        // idle
        else {
            // each drawcomponent has an idle animation, so no check is needed
            if (vsd.dc.isCurrentAnimation(CoreAnimations.IDLE_LEFT)
                    || vsd.dc.isCurrentAnimation(CoreAnimations.RUN_LEFT))
                vsd.dc.setCurrentAnimation(CoreAnimations.IDLE_LEFT);
            else vsd.dc.setCurrentAnimation(CoreAnimations.IDLE_RIGHT);
        }
    }

    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}
}
