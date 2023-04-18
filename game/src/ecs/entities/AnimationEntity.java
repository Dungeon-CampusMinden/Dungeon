package ecs.entities;

import ecs.components.PositionComponent;
import ecs.components.animation.AnimationComponent;
import graphic.Animation;
import starter.Game;
import tools.Point;

public class AnimationEntity extends Entity {

    private Point location;
    private Animation animation;

    /**
     * Creates a new entity animation. An entity used to display animations in the dungeon.
     *
     * @param animation {@link Animation}
     * @param location {@link Point}
     */
    public AnimationEntity(Animation animation, Point location) {
        super();
        this.animation = animation;
        this.location = location;
        this.setupComponents();
    }

    private void setupComponents() {
        new PositionComponent(this, this.location);
        AnimationComponent animationComponent = new AnimationComponent(this, this.animation);

        animationComponent.setOnAnimationEnd(
                () -> {
                    Game.removeEntity(this);
                });
    }

    /**
     * Sets the animation of the entity
     *
     * @param animation {@link Animation}
     */
    public void setAnimation(Animation animation) {
        this.animation = animation;
        getComponent(AnimationComponent.class)
                .ifPresent(ac -> ((AnimationComponent) ac).setCurrentAnimation(animation));
    }

    /**
     * Sets the location of the entity
     *
     * @param location {@link Point}
     */
    public void setLocation(Point location) {
        this.location = location;
        getComponent(PositionComponent.class)
                .ifPresent(pc -> ((PositionComponent) pc).setPosition(location));
    }

    /**
     * Returns the animation of the entity
     *
     * @return {@link Animation}
     */
    public Animation getAnimation() {
        return this.animation;
    }

    /**
     * Returns the location of the entity
     *
     * @return {@link Point}
     */
    public Point getLocation() {
        return this.location;
    }
}
