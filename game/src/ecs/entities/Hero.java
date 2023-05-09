package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.*;
import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
import ecs.graphic.Animation;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity implements IOnDeathFunction, ILevelUp {

    private final int fireballCoolDown = 1;
    private final int healSkillCoolDown = 1;

    private final int enrageCoolDown = 30;
    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;

    private int dmg = 10;

    private final String pathToIdleLeft = "knight/idleLeft";
    private final String pathToIdleRight = "knight/idleRight";
    private final String pathToRunLeft = "knight/runLeft";
    private final String pathToRunRight = "knight/runRight";

    private final String onHit = "knight/hit";

    private SkillComponent sCp;
    private HealthComponent hp;
    private Skill firstSkill;
    private Skill secondSkill;
    private Skill thirdSkill;

    private PlayableComponent pc;
    private XPComponent xpCmp;


    /** Entity with Components */
    public Hero() {
        super();
        new PositionComponent(this);
        setupVelocityComponent();
        setupSkillComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        this.pc = new PlayableComponent(this);
        setupFireballSkill();
        setupHealthComponent();
        setupXpComponent();
        pc.setSkillSlot1(firstSkill);





        this.hp.setCurrentHealthpoints(50); //Set to 50 for testing purposes
    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    private void setupSkillComponent(){
        sCp = new SkillComponent(this);
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

    private void setupHealSkill(){
        secondSkill = new Skill(new HealSkill(), healSkillCoolDown);
        sCp.addSkill(secondSkill);
    }

    private void setupEnrageSkill(){
        thirdSkill = new Skill(new EnrageSkill(), enrageCoolDown);
        sCp.addSkill(thirdSkill);
    }

    /** Modifies the current health by passed amount **/
    public void setHealth(int amount){
        System.out.println("HP before: " + this.hp.getCurrentHealthpoints());
        this.hp.setCurrentHealthpoints(this.hp.getCurrentHealthpoints()+amount);
        System.out.println("HP after: " + this.hp.getCurrentHealthpoints());
    }


    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }

    public void setDmg(int dmg){
        this.dmg = dmg;
    }

    public int getDmg(){
        return dmg;
    }

    //TODO: Fix death animation
    private void setupHealthComponent(){
        Animation hit = AnimationBuilder.buildAnimation(onHit);
        this.hp = new HealthComponent(this, 100, this::onDeath ,hit,hit);
    }

    private void setupXpComponent(){
        xpCmp = new XPComponent(this, this);
    }

    public XPComponent getXpCmp() {
        return xpCmp;
    }

    public void setXpCmp(XPComponent xpCmp) {
        this.xpCmp = xpCmp;
    }



    @Override
    public void onDeath(Entity entity) {
        System.out.println("Hero dead");
    }

    /**
     * LevelUp function that currently adds Skills if needed level is reached.
     *
     * @param nexLevel is the new level of the entity
     */
    @Override
    public void onLevelUp(long nexLevel) {
        System.out.println("You leveled up to Level " + nexLevel);
        if(nexLevel == 1){
            setupHealSkill();
            pc.setSkillSlot2(secondSkill);
        }
        if(nexLevel == 2){
            setupEnrageSkill();
            pc.setSkillSlot3(thirdSkill);
        }
    }
}
