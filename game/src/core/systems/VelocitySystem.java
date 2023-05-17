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
import core.utils.components.draw.Animation;

import java.util.concurrent.atomic.AtomicBoolean;

/** MovementSystem is a system that updates the position of entities */
public class VelocitySystem extends System {

    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}

    @Override
    protected boolean accept(Entity entity) {
        if (entity.getComponent(VelocityComponent.class).isPresent())
            if (entity.getComponent(PositionComponent.class).isPresent())
                if (entity.getComponent(DrawComponent.class).isPresent()) return true;
                else logMissingComponent(entity, DrawComponent.class);
            else logMissingComponent(entity, PositionComponent.class);
        return false;
    }
    /** Updates the position of all entities based on their velocity */
    public void systemUpdate() {
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

        Animation newCurrentAnimation;
        float x = vsd.vc.getCurrentXVelocity();
        if (x > 0) newCurrentAnimation = vsd.vc.getMoveRightAnimation();
        else if (x < 0) newCurrentAnimation = vsd.vc.getMoveLeftAnimation();
        // idle
        else {
            if (vsd.dc.getCurrentAnimation() == vsd.dc.getIdleLeft()
                    || vsd.dc.getCurrentAnimation() == vsd.vc.getMoveLeftAnimation())
                newCurrentAnimation = vsd.dc.getIdleLeft();
            else newCurrentAnimation = vsd.dc.getIdleRight();
        }
        vsd.dc.setCurrentAnimation(newCurrentAnimation);
    }
}
