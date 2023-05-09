package ecs.components.skill;

import ecs.components.VelocityComponent;
import ecs.entities.Entity;

public class SpeedboostSkill extends BuffSkill {

    private final float factor;
    private float oldX = 0;
    private float oldY = 0;

    /**
     * @param factor by which the speed will be changed by
     */
    public SpeedboostSkill(float factor) {
        this.factor = factor;
    }

    @Override
    public void execute(Entity entity) {
        // increase speed, if it hasn't already been
        if (this.oldX == 0 && this.oldY == 0) {
            entity.getComponent(VelocityComponent.class)
                .ifPresent(c -> {
                    VelocityComponent vc = (VelocityComponent) c;
                    this.oldX = vc.getXVelocity();
                    this.oldY = vc.getYVelocity();
                    vc.setXVelocity(oldX * this.factor);
                    vc.setYVelocity(oldY * this.factor);
                });
        // else revert to old speed
        } else {
            entity.getComponent(VelocityComponent.class)
                .ifPresent(c -> {
                    VelocityComponent vc = (VelocityComponent) c;
                    vc.setXVelocity(oldX);
                    vc.setYVelocity(oldY);
                    oldX = 0;
                    oldY = 0;
                });
        }
    }
}
