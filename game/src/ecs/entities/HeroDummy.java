package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import graphic.Animation;
import tools.Point;

public class HeroDummy extends Entity{

    public HeroDummy(Point startPosition){
        super();
        new PositionComponent(this, startPosition);
        //new MultiplayerComponent(this, playerId);
        setupAnimationComponent();
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation("knight/idleRight");
        Animation idleLeft = AnimationBuilder.buildAnimation("knight/idleLeft");
//        Animation moveRight = AnimationBuilder.buildAnimation("knight/runRight");
//        Animation moveLeft = AnimationBuilder.buildAnimation("knight/runLeft");

        new AnimationComponent(this, idleLeft, idleRight);

        //new VelocityComponent(this, 0.3f, 0.3f, moveLeft, moveRight);
    }
}
