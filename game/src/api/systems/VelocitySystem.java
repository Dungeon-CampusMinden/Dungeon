package api.systems;

import api.System;
import api.components.AnimationComponent;
import api.components.MissingComponentException;
import api.components.PositionComponent;
import api.components.VelocityComponent;
import api.components.ProjectileComponent;
import api.Entity;
import content.utils.animation.Animation;
import starter.Game;
import content.utils.position.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** MovementSystem is a system that updates the position of entities */
public class VelocitySystem extends System {

    Set<Entity> entities;
    public void accept(Entity entity){
        //build vs data ??
  //return true wenn ich mag den
        //hast VC und PC und AC -> GO
        //hast ken VC -> bist mir egal
        //fehlt was außer das VC? -> Logging
    }


    private record VSData(Entity e, VelocityComponent vc, PositionComponent pc) {}

    /** Updates the position of all entities based on their velocity */
    public void update() {
for each meineenti.updat();
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

        AnimationComponent ac =
                (AnimationComponent)
                        entity.getComponent(AnimationComponent.class).get();

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
