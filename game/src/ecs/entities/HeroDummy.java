package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.mp.*;
import graphic.Animation;
import tools.Point;

public class HeroDummy extends Entity{

    public HeroDummy(Point startPosition, int playerId){
        super();
        new PositionComponent(this, startPosition);
        new MultiplayerComponent(this, playerId);
        setupAnimationComponent();
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation("knight/idleRight");
        Animation idleLeft = AnimationBuilder.buildAnimation("knight/idleLeft");
        new AnimationComponent(this, idleLeft, idleRight);
    }
}
