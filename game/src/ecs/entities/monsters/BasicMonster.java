package ecs.entities.monsters;

import ecs.entities.Entity;


public abstract class BasicMonster extends Entity {
    protected final float xSpeed;
    protected final float ySpeed;
    protected final float hp;

    protected String pathToIdleLeft;
    protected String pathToIdleRight;
    protected String pathToRunLeft;
    protected String pathToRunRight;

    public BasicMonster(float xSpeed, float ySpeed, float hp, String pathToIdleLeft, String pathToIdleRight, String pathToRunLeft, String pathToRunRight) {
        super();
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.hp = hp;
        this.pathToIdleLeft = pathToIdleLeft;
        this.pathToIdleRight = pathToIdleRight;
        this.pathToRunLeft = pathToRunLeft;
        this.pathToRunRight = pathToRunRight;

    }

    public abstract void setupVelocityComponent();

    public abstract void setupAnimationComponent();

    public abstract void setupAIComponent();


}
