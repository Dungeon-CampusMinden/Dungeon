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
import core.utils.components.draw.CoreAnimations;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MovementSystem is a system that updates the position of entities
 */
public class VelocitySystem extends System {

    public VelocitySystem() {
        super(VelocityComponent.class, PositionComponent.class, DrawComponent.class);
    }

    /**
     * Updates the position of all entities based on their velocity
     */
    @Override
    public void execute() {
        getEntityStream().map(this::buildDataObject).forEach(this::updatePosition);
    }

    private void updatePosition(VSData vsd) {
        float newX = vsd.pc.getPosition().x + vsd.vc.getCurrentXVelocity();
        float newY = vsd.pc.getPosition().y + vsd.vc.getCurrentYVelocity();
        Point newPosition = new Point(newX, newY);
        if (Game.currentLevel.getTileAt(newPosition.toCoordinate()).isAccessible()) {
            vsd.pc.setPosition(newPosition);
            movementAnimation(vsd);
        }

        // remove projectiles that hit the wall or other non-accessible
        // tiles
        else if (vsd.e.getComponent(ProjectileComponent.class).isPresent())
            Game.removeEntity(vsd.e);

        vsd.vc.setCurrentYVelocity(0);
        vsd.vc.setCurrentXVelocity(0);
    }

    private VSData buildDataObject(Entity e) {
        VelocityComponent vc = (VelocityComponent) e.getComponent(VelocityComponent.class).get();

        PositionComponent pc = (PositionComponent) e.getComponent(PositionComponent.class).get();

        DrawComponent dc = (DrawComponent) e.getComponent(DrawComponent.class).get();

        return new VSData(e, vc, pc, dc);
    }

    private void movementAnimation(VSData vsd) {

        AtomicBoolean isDead = new AtomicBoolean(false);
        vsd.e
            .getComponent(HealthComponent.class)
            .ifPresent(
                component -> {
                    HealthComponent healthComponent = (HealthComponent) component;
                    isDead.set(healthComponent.isDead());
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
            if (vsd.dc.getCurrentAnimation() == vsd.dc.getAnimation(CoreAnimations.IDLE_LEFT).get()
                || (vsd.dc.hasAnimation(CoreAnimations.RUN_LEFT)
                && vsd.dc.getCurrentAnimation()
                == vsd.dc.getAnimation(CoreAnimations.RUN_LEFT).get()))
                vsd.dc.setCurrentAnimation(CoreAnimations.IDLE_LEFT);
            else vsd.dc.setCurrentAnimation(CoreAnimations.IDLE_RIGHT);
        }
    }

    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {
    }
}
