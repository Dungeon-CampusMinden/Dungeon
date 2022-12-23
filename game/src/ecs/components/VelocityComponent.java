package ecs.components;

import ecs.entitys.Entity;
import mydungeon.ECS;

/** VelocityComponent is a component that stores the x, y movement direction */
public class VelocityComponent implements Component {

    private float x;
    private float y;
    private float xSpeed;
    private float ySpeed;

    /**
     * @param entity associated entity
     * @param x x coordinate
     * @param y y coordinate
     */
    public VelocityComponent(Entity entity, float x, float y, float xSpeed, float ySpeed) {
        ECS.velocityComponentMap.put(entity, this);
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }

    /**
     * @return x movement
     */
    public float getX() {
        return x;
    }

    /**
     * @param x set x coordinate
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @return y movement
     */
    public float getY() {
        return y;
    }

    /**
     * @param y set y coordinate
     */
    public void setY(float y) {
        this.y = y;
    }

    public float getxSpeed() {
        return xSpeed;
    }

    public void setxSpeed(float xSpeed) {
        this.xSpeed = xSpeed;
    }

    public float getySpeed() {
        return ySpeed;
    }

    public void setySpeed(float ySpeed) {
        this.ySpeed = ySpeed;
    }
}
