package core.systems;

import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;

import core.Component;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.draw.Animation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** MovementSystem is a system that updates the position of entities */
public class VelocitySystem extends System {

    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}

    public VelocitySystem() {
        super(VelocityComponent.class, getSet());
    }

    private static Set<Class<? extends Component>> getSet() {
        Set<Class<? extends Component>> set = new HashSet<>();
        set.add(PositionComponent.class);
        set.add(DrawComponent.class);
        return set;
    }
    /** Updates the position of all entities based on their velocity */
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
