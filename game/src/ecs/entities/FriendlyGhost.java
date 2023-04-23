package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.FriendlyTransition;
import ecs.graphic.Animation;

import java.util.Random;

/**
 * A friendly ghost npc is a friendly npc that has certain chance to spawn.
 *
 *
 * It is possible that the ghost is just idling instead of following the hero.
 * The ghost has a chance to spawn a grave, if the grave is found by the hero
 * he will get rewarded. (As long as the ghost follows and isn't idling)
 *
 *
 *
 */
public class FriendlyGhost extends Entity{

    private final float xSpeed = 0.2f;
    private final float ySpeed = 0.2f;

    private boolean disappear = false;

    private boolean follow;
    private Grave grave;

    private Hero hero;

    private final String pathToIdleLeft = "monster/ghost";
    private final String pathToIdleRight = "monster/ghost";
    private final String pathToRunLeft = "monster/ghost";
    private final String pathToRunRight = "monster/ghost";

    /** Constructor
     *
     * @param hero - when initialized it needs to get passed the hero, so that it knows who to reward if a grave is found
     * */
    public FriendlyGhost(Hero hero){
        super();
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupAi();
        this.hero = hero;

        //80% chance of spawning a grave
        if(new Random().nextInt(0,100)>20) spawnGrave();

    }

    /**If ghost follows hero the hero will receive 10hp**/
    public void reward(){
        if (follow) this.hero.setHealth(10);
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
            (you, other, direction) -> System.out.println("ghostCollisionEnter"),
            (you, other, direction) -> System.out.println("ghostCollisionLeave"));
    }

    /** **/
    private void setupFriendlyIdleAiComponent(){
        new AIComponent(this, new CollideAI(5f),new RadiusWalk(5,2),new FriendlyTransition());
    }

    private void spawnGrave(){
        this.grave = new Grave(this);
    }


    /**
     * Setups the AI Component by determining its current state
     * There is a 15% chance that the ghost is just idling in the level
     * **/
    private void setupAi(){
        Random random = new Random();
        int rng = random.nextInt(100-0);

        if(rng>15){
            new AIComponent(this);
            this.follow = true;
            System.out.println("GHOST SPAWN");
        } else{
            setupFriendlyIdleAiComponent();
            this.follow = false;
            System.out.println("GHOST IDLE");
        }
    }
}
