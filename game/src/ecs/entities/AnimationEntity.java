package ecs.entities;

import ecs.components.PositionComponent;
import ecs.components.animation.AnimationComponent;
import graphic.Animation;
import java.util.logging.Logger;
import starter.Game;
import tools.Point;

public class AnimationEntity extends Entity {

    private static final Logger logger = Logger.getLogger(AnimationEntity.class.getName());

    /**
     * Creates a new entity animation. An entity used to display animations in the dungeon.
     *
     * @param animation {@link Animation}
     * @param location {@link Point}
     */
    public AnimationEntity(Animation animation, Point location) {
        super();
        this.setupComponents(animation, location);
        if (animation.isLooping()) {
            logger.warning(
                    "Looping animation used with AnimationEntity. Looping animations will never be removed from the game. Except manually via Game.removeEntity(Entity).");
        }
    }

    private void setupComponents(Animation animation, Point location) {
        new PositionComponent(this, location);
        AnimationComponent animationComponent = new AnimationComponent(this, animation);

        animationComponent.setOnAnimationEnd(
                () -> {
                    Game.removeEntity(this);
                });
    }
}
