package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.skill.*;
import ecs.damage.Damage;
import ecs.components.skill.ExplosivePebbleSkill;
import graphic.Animation;
import ecs.components.OnDeathFunctions.EndGame;
import ecs.components.quests.QuestComponent;

import java.io.Serializable;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to
 * setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity implements Serializable {

    private final int explosivePebbleCoolDown = 1;
    private final int stabCoolDown = 1;
    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;
    private final int maxHealth = 100;

    private final String pathToIdleLeft = "knight/idleLeft";
    private final String pathToIdleRight = "knight/idleRight";
    private final String pathToRunLeft = "knight/runLeft";
    private final String pathToRunRight = "knight/runRight";
    private final String pathToGetHit = "knight/getHit";
    private final String pathToDie = "knight/die";
    private Skill firstSkill;
    private Skill secondSkill;

    /** Entity with Components */
    public Hero() {
        super();
        setupComponents(maxHealth, maxHealth);
    }

    /** Maybe this will let me load */
    public void setupComponents(int maxHealth, int currentHealth) {
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupHealthComponent(maxHealth, currentHealth);
        PlayableComponent pc = new PlayableComponent(this);
        setupSkillComponent();
        pc.setSkillSlot1(firstSkill);
        pc.setSkillSlot2(secondSkill);
        setupQuestComponent();
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
        firstSkill = new Skill(
                new ExplosivePebbleSkill(SkillTools::getCursorPositionAsPoint, this), explosivePebbleCoolDown);
    }

    private void setupStabSkill() {
        secondSkill = new Skill(
                new StabSkill(SkillTools::getCursorPositionAsPoint, this), stabCoolDown);
    }

    private void setupSkills() {
        setupFireballSkill();
        setupStabSkill();
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }

    private void setupHealthComponent(int maxHealth, int currentHealth) {
        Animation getHit = AnimationBuilder.buildAnimation(pathToGetHit);
        Animation die = AnimationBuilder.buildAnimation(pathToDie);
        IOnDeathFunction gameOver = new EndGame();
        HealthComponent hc = new HealthComponent(this, maxHealth, gameOver, getHit, die);
        hc.setCurrentHealthpoints(currentHealth);
    }

    private void setupSkillComponent() {
        SkillComponent sc = new SkillComponent(this);
        setupSkills();
        sc.addSkill(firstSkill);
        sc.addSkill(secondSkill);
    }

    private void setupQuestComponent() {
        new QuestComponent(this);
    }
}
