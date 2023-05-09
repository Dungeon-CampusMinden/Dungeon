package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.*;
import ecs.graphic.Animation;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity implements IOnDeathFunction {

    private final int fireballCoolDown = 5;
    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;

    private final String pathToIdleLeft = "knight/idleLeft";
    private final String pathToIdleRight = "knight/idleRight";
    private final String pathToRunLeft = "knight/runLeft";
    private final String pathToRunRight = "knight/runRight";

    private final String onHit = "knight/hit";

    private HealthComponent hp;

    private int dmg = 5;
    private Skill firstSkill;
    private InventoryComponent inv;


    int currentHealth;

    /** Entity with Components */
    public Hero() {
        super();
        new PositionComponent(this);
        inv = new InventoryComponent(this,12);


        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();

        PlayableComponent pc = new PlayableComponent(this);

        setupFireballSkill();
        setupHealthComponent();

        pc.setSkillSlot1(firstSkill);

        this.hp.setCurrentHealthpoints(50); //Set to 50 for testing purposes

        currentHealth = this.hp.getCurrentHealthpoints();
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

    private void setupFireballSkill() {
        firstSkill =
                new Skill(
                        new FireballSkill(SkillTools::getCursorPositionAsPoint), fireballCoolDown);
    }

    /** Modifies the current health by passed amount **/
    public void setHealth(int amount){
        System.out.println("HP before: " + this.hp.getCurrentHealthpoints());
        this.hp.setCurrentHealthpoints(this.hp.getCurrentHealthpoints()+amount);
        System.out.println("HP after: " + this.hp.getCurrentHealthpoints());
    }


    public int getDmg() {
        return dmg;
    }

    public void setDmg(int dmg) {
        this.dmg = dmg;
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> System.out.print(""),
                (you, other, direction) -> System.out.print(""));
    }

    //TODO: Fix death animation
    private void setupHealthComponent(){
        Animation hit = AnimationBuilder.buildAnimation(onHit);
        this.hp = new HealthComponent(this, 100, this::onDeath ,hit,hit);
    }

    @Override
    public void onDeath(Entity entity) {
        System.out.println("Hero dead");
    }

    //======================================GETTER&SETTER=====================================================//

    public InventoryComponent getInv() {
        return inv;
    }

    public void setInv(InventoryComponent inv) {
        this.inv = inv;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }
}
