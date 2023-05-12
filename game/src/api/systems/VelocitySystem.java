package api.systems;

import api.Entity;
import api.Game;
import api.System;
import api.components.DrawComponent;
import api.components.PositionComponent;
import api.components.VelocityComponent;
import api.utils.Point;
import api.utils.component_utils.MissingComponentException;
import api.utils.component_utils.drawComponent.Animation;
import content.component.HealthComponent;
import content.component.ProjectileComponent;
import java.util.concurrent.atomic.AtomicBoolean;

/** MovementSystem is a system that updates the position of entities */
public class VelocitySystem extends System {

    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc) {}

    /** Updates the position of all entities based on their velocity */
    public void update() {
        Game.getEntities().stream()
                .flatMap(e -> e.getComponent(VelocityComponent.class).stream())
                .map(vc -> buildDataObject((VelocityComponent) vc))
                .forEach(this::updatePosition);
    }

    private VSData updatePosition(VSData vsd) {
        float newX = vsd.pc.getPosition().x + vsd.vc.getCurrentXVelocity();
        float newY = vsd.pc.getPosition().y + vsd.vc.getCurrentYVelocity();
        Point newPosition = new Point(newX, newY);
        if (Game.currentLevel.getTileAt(newPosition.toCoordinate()).isAccessible()) {
            vsd.pc.setPosition(newPosition);
            movementAnimation(vsd.e);
        }

        // remove projectiles that hit the wall or other non-accessible
        // tiles
        else if (vsd.e.getComponent(ProjectileComponent.class).isPresent())
            Game.removeEntity(vsd.e);

        vsd.vc.setCurrentYVelocity(0);
        vsd.vc.setCurrentXVelocity(0);

        return vsd;
    }

    private VSData buildDataObject(VelocityComponent vc) {
        Entity e = vc.getEntity();

        PositionComponent pc =
                (PositionComponent)
                        e.getComponent(PositionComponent.class)
                                .orElseThrow(VelocitySystem::missingPC);

        return new VSData(e, vc, pc);
    }

    private void movementAnimation(Entity entity) {

        AtomicBoolean isDead = new AtomicBoolean(false);
        entity.getComponent(HealthComponent.class)
                .ifPresent(
                        component -> {
                            HealthComponent healthComponent = (HealthComponent) component;
                            isDead.set(healthComponent.isDead());
                        });

        if (isDead.get()) {
            return;
        }

        DrawComponent ac =
                (DrawComponent)
                        entity.getComponent(DrawComponent.class)
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
