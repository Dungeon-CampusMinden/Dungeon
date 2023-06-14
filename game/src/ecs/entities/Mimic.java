package ecs.entities;


import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.MimicAI;
import ecs.components.ai.idle.MimicWalk;
import ecs.components.ai.transition.RangeTransition;
import ecs.components.skill.FireballSkill;
import ecs.components.skill.ITargetSelection;
import ecs.components.skill.Skill;
import ecs.components.skill.SkillComponent;
import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import graphic.Animation;
import starter.Game;
import tools.Point;


import java.util.logging.Logger;

public class Mimic extends Monster{

    private static transient final Logger mimicLogger = Logger.getLogger(Mimic.class.getName());

    private final int maxHealth = 100;
    private int attackCooldown = 2;
    private int level;
    private boolean attacking = false;
    private final String pathToClosedChest = "monster/Mimic/closedChest";
    private final String pathToOpenChest = "monster/Mimic/openChest";

    private XPComponent xPComponent;

    public Mimic(int level) {
        super(level);
        this.level = level;
        new PositionComponent(this);
        setupAnimationComponent();
        setupHealthComponent();
        setupDamageComponent();
        setupAIComponent();
        setupInteractionComponent();
        mimicLogger.info("Mimic created");
    }

    private void setupAIComponent() {
        FireballSkill s1 = new FireballSkill(new ITargetSelection() {
            @Override
            public Point selectTargetPoint() {
                return entityPosition();
            }
        },
            this);

        SkillComponent sc = new SkillComponent(this);
        Skill skill1 = new Skill(s1, attackCooldown);
        sc.addSkill(skill1);
        AIComponent a = new AIComponent(this);
        a.setIdleAI(new MimicWalk());
        a.setTransitionAI(new RangeTransition(100f));
        a.setFightAI(new MimicAI(skill1));
    }

    private Point entityPosition() {
        mimicLogger.info("Boss position requested");
        if (!Game.getHero().isPresent())
            return null;
        return ((PositionComponent) Game.getHero().get().getComponent(PositionComponent.class)
            .orElseThrow(
                () -> new MissingComponentException(
                    "PositionComponent")))
            .getPosition();

    }


    private void setupAnimationComponent() {
        Animation openChest = AnimationBuilder.buildAnimation(pathToOpenChest);
        Animation closeChest = AnimationBuilder.buildAnimation(pathToClosedChest);
        new AnimationComponent(this, openChest, closeChest);
    }

    private void setupHealthComponent() {
        IOnDeathFunction iOnDeathFunction = new IOnDeathFunction() {
            public void onDeath(Entity entity) {
                // Create a treasure chest
                Chest treasureChest = Chest.createNewChest();
            }
        };
        HealthComponent healthComponenet = new HealthComponent(this);
        healthComponenet.setOnDeath(iOnDeathFunction);
        healthComponenet.setMaximalHealthpoints(maxHealth);
    }

    private void setupDamageComponent() {
        new DamageComponent(this, calcDamage());
    }

    private void setupInteractionComponent() {
        new InteractionComponent(this, 5, false, Mimic::mimicInteraction);
        getAttacking();

    }


    private int calcDamage() {
        return 5 + (int) Math.sqrt(10 * xPComponent.getCurrentLevel());
    }


    public static void mimicInteraction(Entity entity) {
        mimicLogger.info("Mimic attacked");

    }

    public boolean getAttacking(){
        return attacking;
    }
}
