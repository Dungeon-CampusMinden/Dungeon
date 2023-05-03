package ecs.entities.objects;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.InteractionComponent;
import ecs.entities.Entity;
import ecs.entities.nps.Ghost;
import graphic.Animation;


public class Tombstone extends Entity {


    private final String pathToTombstone = "objects/treasurechest/Object/Tombstone.png";
    Ghost ghost = new Ghost();

    public Tombstone() {
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupInteractionComponent();
    }

    private void setupAnimationComponent() {
        Animation stone = AnimationBuilder.buildAnimation(pathToTombstone);
        new AnimationComponent(this, stone);
    }

    private void setupInteractionComponent() {
        new InteractionComponent(this, InteractionComponent.DEFAULT_RADIUS, true, (playerEntity) -> {


        });
    }
}
