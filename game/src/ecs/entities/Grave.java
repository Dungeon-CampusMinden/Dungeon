package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.collision.ICollide;
import graphic.Animation;
import level.elements.tile.Tile;

public class Grave extends Entity implements ICollide {
    private final String pathToIdleRight = "dungeon/gravestone";
    private final FriendlyGhost ghost;

    public Grave(FriendlyGhost ghost){
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupHitboxComponent();
        this.ghost = ghost;
    }
    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        new AnimationComponent(this, idleRight);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> System.out.println("heroCollisionEnter"),
            (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }

    @Override
    public void onCollision(Entity a, Entity b, Tile.Direction from) {
        //TODO: Kollision mit Geist - if(b instanceof FriendlyGhost)
    }
}
