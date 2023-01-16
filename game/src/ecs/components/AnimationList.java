package ecs.components;

import graphic.Animation;

/** List of all possible animations for a entity */
public class AnimationList {

    private Animation idleRight;
    private Animation idleLeft;
    private Animation moveLeft;
    private Animation moveRight;
    private Animation moveUp;
    private Animation moveDown;
    private Animation getHit;
    private Animation die;

    public void setIdleRight(Animation animation) {
        idleRight = animation;
    }

    public void setIdleLeft(Animation animation) {
        idleLeft = animation;
    }

    public void setMoveRight(Animation animation) {
        moveRight = animation;
    }

    public void setMoveLeft(Animation animation) {
        moveLeft = animation;
    }

    public void setMoveUp(Animation animation) {
        moveUp = animation;
    }

    public void setMoveDown(Animation animation) {
        moveDown = animation;
    }

    public void setGetHit(Animation animation) {
        getHit = animation;
    }

    public void setDie(Animation animation) {
        die = animation;
    }

    public Animation getIdleRight() {
        return idleRight;
    }

    public Animation getIdleLeft() {
        return idleLeft;
    }

    public Animation getMoveLeft() {
        return moveLeft;
    }

    public Animation getMoveRight() {
        return moveRight;
    }

    public Animation getMoveDown() {
        return moveDown;
    }

    public Animation getMoveUp() {
        return moveUp;
    }

    public Animation getGetHit() {
        return getHit;
    }

    public Animation getDie() {
        return die;
    }
}
