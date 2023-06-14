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
import ecs.items.ItemData;
import graphic.Animation;
import starter.Game;
import tools.Point;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Mimic extends Monster{

    private static transient final Logger mimicLogger = Logger.getLogger(Mimic.class.getName());

    private final int maxHealth = 20;
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
        setupXPComponent();
        setupDamageComponent();
        setupAIComponent();
        setupInteractionComponent();
        setupHitBoxComponent();
        mimicLogger.info("Mimic created");
    }

    private void setupXPComponent() {
        xPComponent = new XPComponent(this, new ILevelUp() {

            @Override
            public void onLevelUp(long nexLevel) {
                HealthComponent health = (HealthComponent) getComponent(HealthComponent.class).get();
                health.setMaximalHealthpoints((int) (health.getMaximalHealthpoints() * 1.01f));
                health.setCurrentHealthpoints(health.getMaximalHealthpoints());
                xPComponent.setLootXP(50 * (int) nexLevel);
                ((DamageComponent) getComponent(DamageComponent.class).get()).setDamage(calcDamage());
            }

        }, 5 * level);
        xPComponent.setCurrentLevel(level / 10);
        ((HealthComponent) getComponent(HealthComponent.class).get())
            .setMaximalHealthpoints(maxHealth * (int) Math.pow(1.01f, xPComponent.getCurrentLevel()));
        ((HealthComponent) getComponent(HealthComponent.class).get())
            .setCurrentHealthpoints(maxHealth * (int) Math.pow(1.01f, xPComponent.getCurrentLevel()));
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
        if (Game.getHero().isEmpty())
            return null;
        return ((PositionComponent) Game.getHero().get().getComponent(PositionComponent.class)
            .orElseThrow(
                () -> new MissingComponentException(
                    "PositionComponent")))
            .getPosition();

    }
    private void setupHitBoxComponent(){
        new HitboxComponent(this);
    }

    private void setupAnimationComponent() {
        Animation openChest = AnimationBuilder.buildAnimation(pathToOpenChest);
        Animation closeChest = AnimationBuilder.buildAnimation(pathToClosedChest);
        new AnimationComponent(this, closeChest, openChest);
    }

    private void setupHealthComponent() {
        IOnDeathFunction iOnDeathFunction = new IOnDeathFunction() {
            public void onDeath(Entity entity) {
                // Create a treasure chest
                List<ItemData> items = new ArrayList<>(4);
                items.add(SpeedPotion.getItemData());
                items.add(Cake.getItemData());
                items.add(MonsterPotion.getItemData());
                items.add(Bag.getItemData());
                PositionComponent positionComponent = (PositionComponent) entity.getComponent(PositionComponent.class).get();
                Chest chest = new Chest(items, positionComponent.getPosition());
            }
        };
        new HealthComponent(this, maxHealth, iOnDeathFunction, AnimationBuilder.buildAnimation(this.pathToClosedChest), AnimationBuilder.buildAnimation(this.pathToClosedChest));
    }

    private void setupDamageComponent() {
        new DamageComponent(this, calcDamage());
    }

    private void setupInteractionComponent() {
        IInteraction iInteraction = new IInteraction() {
            @Override
            public void onInteraction(Entity entity) {
                 if(entity instanceof Mimic)
                     ((Mimic) entity).attacking = true;
            }
        };
        new InteractionComponent(this, 1, false, iInteraction);
    }

    private int calcDamage() {
        return 5 + (int) Math.sqrt(10 * xPComponent.getCurrentLevel());
    }

    public boolean getAttacking(){
        return attacking;
    }
}
