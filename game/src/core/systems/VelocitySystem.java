package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

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
 * <p>The system will take the {@link VelocityComponent#currentXVelocity()} and {@link
 * VelocityComponent#currentYVelocity()} and calculate the new position of the entity based on their
 * current position stored in the {@link PositionComponent}. If the new position is a valid
 * position, which means the tile they would stand on is accessible, the new position will be set.
 *
 * <p>This system will also set the current animation to {@link CoreAnimations#RUN_LEFT} or {@link
 * CoreAnimations#RUN_RIGHT} if the position is valid.
 *
 * <p>If the new position is not valid, the {@link CoreAnimations#IDLE_LEFT} or {@link
 * CoreAnimations#IDLE_RIGHT} animations will be set as the new current animation.
 *
 * <p>At the end, the {@link VelocityComponent#currentXVelocity(float)} and {@link
 * VelocityComponent#yVelocity(float)} will be set to 0.
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
        entityStream().map(this::buildDataObject).forEach(this::updatePosition);
    }

    private void updatePosition(VSData vsd) {
        Vector2 velocity = new Vector2(vsd.vc.currentXVelocity(), vsd.vc.currentYVelocity());
        float maxSpeed = Math.max(Math.abs(vsd.vc.xVelocity()), Math.abs(vsd.vc.yVelocity()));
        // Limit velocity to maxSpeed (primarily for diagonal movement)
        if (velocity.len() > maxSpeed) {
            velocity.nor();
            velocity.scl(maxSpeed);
        }
        if (Gdx.graphics != null) {
            velocity.scl(Gdx.graphics.getDeltaTime());
        }

        float newX = vsd.pc.position().x + velocity.x;
        float newY = vsd.pc.position().y + velocity.y;

        if (Game.tileAT(new Point(newX, newY)).isAccessible()) {
            vsd.pc.position(new Point(newX, newY));
            this.movementAnimation(vsd);
        } else if (Game.tileAT(new Point(newX, vsd.pc.position().y)).isAccessible()) {
            vsd.pc.position(new Point(newX, vsd.pc.position().y));
            this.movementAnimation(vsd);
            vsd.vc.currentYVelocity(0.0f);
        } else if (Game.tileAT(new Point(vsd.pc.position().x, newY)).isAccessible()) {
            vsd.pc.position(new Point(vsd.pc.position().x, newY));
            this.movementAnimation(vsd);
            vsd.vc.currentXVelocity(0.0f);
        }

        // remove projectiles that hit the wall or other non-accessible
        // tiles
        else if (vsd.e.fetch(ProjectileComponent.class).isPresent()) Game.remove(vsd.e);

        float friction = Game.tileAT(vsd.pc.position()).friction();
        float newVX = vsd.vc.currentXVelocity() * (Math.min(1.0f, 1.0f - friction));
        if (Math.abs(newVX) < 0.01f) newVX = 0.0f;
        float newVY = vsd.vc.currentYVelocity() * (Math.min(1.0f, 1.0f - friction));
        if (Math.abs(newVY) < 0.01f) newVY = 0.0f;

        vsd.vc.currentYVelocity(newVY);
        vsd.vc.currentXVelocity(newVX);
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

        float x = vsd.vc.currentXVelocity();
        float y = vsd.vc.currentYVelocity();
        if (x > 0) vsd.dc.nextAnimation(CoreAnimations.RUN_RIGHT, CoreAnimations.RUN);
        else if (x < 0) vsd.dc.nextAnimation(CoreAnimations.RUN_LEFT, CoreAnimations.RUN);
        else if (y > 0) vsd.dc.nextAnimation(CoreAnimations.RUN_UP, CoreAnimations.RUN);
        else if (y < 0) vsd.dc.nextAnimation(CoreAnimations.RUN_DOWN, CoreAnimations.RUN);
        // idle
        else {
            // each drawComponent has an idle animation, so no check is needed
            if (vsd.dc.isCurrentAnimation(CoreAnimations.IDLE_LEFT)
                    || vsd.dc.isCurrentAnimation(CoreAnimations.RUN_LEFT))
                vsd.dc.nextAnimation(
                        2,
                        CoreAnimations.IDLE_LEFT,
                        CoreAnimations.IDLE,
                        CoreAnimations.IDLE_RIGHT,
                        CoreAnimations.IDLE_DOWN,
                        CoreAnimations.IDLE_UP);
            else if (vsd.dc.isCurrentAnimation(CoreAnimations.IDLE_RIGHT)
                    || vsd.dc.isCurrentAnimation(CoreAnimations.RUN_RIGHT))
                vsd.dc.nextAnimation(
                        2,
                        CoreAnimations.IDLE_RIGHT,
                        CoreAnimations.IDLE,
                        CoreAnimations.IDLE_LEFT,
                        CoreAnimations.IDLE_DOWN,
                        CoreAnimations.IDLE_UP);
            else if (vsd.dc.isCurrentAnimation(CoreAnimations.IDLE_UP)
                    || vsd.dc.isCurrentAnimation(CoreAnimations.RUN_DOWN))
                vsd.dc.nextAnimation(
                        2,
                        CoreAnimations.IDLE_UP,
                        CoreAnimations.IDLE,
                        CoreAnimations.IDLE_DOWN,
                        CoreAnimations.IDLE_LEFT,
                        CoreAnimations.IDLE_RIGHT);
            else
                vsd.dc.nextAnimation(
                        2,
                        CoreAnimations.IDLE_DOWN,
                        CoreAnimations.IDLE,
                        CoreAnimations.IDLE_UP,
                        CoreAnimations.IDLE_LEFT,
                        CoreAnimations.IDLE_RIGHT);
        }
    }

    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc, DrawComponent dc) {}
}
