package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.fight.MeleeAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.FriendlyTransition;
import ecs.components.ai.transition.ITransition;
import graphic.Animation;

import java.util.Random;

public class FriendlyGhost extends Entity{

    private final float xSpeed = 0.2f;
    private final float ySpeed = 0.2f;

    private boolean disappear = false;
    private Grave grave;

    private AIComponent ai;

    private final String pathToIdleLeft = "monster/ghost";
    private final String pathToIdleRight = "monster/ghost";
    private final String pathToRunLeft = "monster/ghost";
    private final String pathToRunRight = "monster/ghost";

    public FriendlyGhost(Entity hero){
        super();
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupAi();
        if(new Random().nextBoolean()) spawnGrave();

    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> System.out.println("heroCollisionEnter"),
            (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }

    private void setupFriendlyIdleAiComponent(){
        new AIComponent(this, new CollideAI(5f),new RadiusWalk(5,5),new FriendlyTransition());
    }

    private void spawnGrave(){
        this.grave = new Grave(this);
    }
    //Determines current state
    public void setupAi(){
        Random random = new Random();

        //If true will follow the hero - else it will roll again
        if(random.nextBoolean()){
            new AIComponent(this);
            System.out.println("GHOST SPAWN");
        } else{
            setupFriendlyIdleAiComponent();
            System.out.println("GHOST IDLE");
        }
    }

    public boolean getDisappear(){
        return this.disappear;
    }
}
