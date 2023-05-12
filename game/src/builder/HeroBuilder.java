package builder;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.*;
import ecs.entities.Entity;
import graphic.Animation;

/**
 * This class can be used to build an Entity that behaves like a playable hero character.
 *
 * <p>The Hero is the player character. It's an entity in the ECS. This class helps to set up the
 * hero with all its components and attributes.
 */
public class HeroBuilder {

    private static final int fireballCoolDown = 5;
    private static final float xSpeed = 0.3f;
    private static final float ySpeed = 0.3f;

    private static final String pathToIdleLeft = "knight/idleLeft";
    private static final String pathToIdleRight = "knight/idleRight";
    private static final String pathToRunLeft = "knight/runLeft";
    private static final String pathToRunRight = "knight/runRight";

    /** Entity with components for a hero */
    public static Entity buildHero() {
        Entity hero = new Entity();
        new PositionComponent(hero);
        setupVelocityComponent(hero);
        setupAnimationComponent(hero);
        setupHitboxComponent(hero);
        PlayableComponent pc = new PlayableComponent(hero);
        pc.setSkillSlot1(
                new Skill(
                        new FireballSkill(SkillTools::getCursorPositionAsPoint), fireballCoolDown));

        return hero;
    }

    private static void setupVelocityComponent(Entity hero) {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(hero, xSpeed, ySpeed, moveLeft, moveRight);
    }

    private static void setupAnimationComponent(Entity hero) {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(hero, idleLeft, idleRight);
    }

    private static void setupHitboxComponent(Entity hero) {
        new HitboxComponent(
                hero,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }
}
