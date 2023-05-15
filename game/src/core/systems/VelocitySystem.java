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
import core.utils.components.draw.Animation;
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
        Point previousPosition = vsd.pc.getPosition();
        float newX = vsd.pc.getPosition().x + vsd.vc.getCurrentXVelocity();
        float newY = vsd.pc.getPosition().y + vsd.vc.getCurrentYVelocity();
        Point newPosition = new Point(newX, newY);
        if (Game.currentLevel.getTileAt(newPosition.toCoordinate()).isAccessible()) {
            vsd.pc.setPosition(newPosition);
            boolean hasPositionChanged = newPosition.x != previousPosition.x || newPosition.y != previousPosition.y;
            if (hasPositionChanged)
                Game.sendPosition();
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
