package content.entity;

import api.ecs.components.*;
import api.ecs.components.skill.FireballSkill;
import api.ecs.components.skill.Skill;
import api.ecs.components.skill.SkillComponent;
import api.ecs.components.skill.SkillTools;
import api.ecs.entities.Entity;
import api.ecs.items.ItemData;
import api.ecs.items.ItemDataGenerator;
import api.graphic.Animation;
import api.level.tools.LevelElement;
import api.utils.Point;
import content.component_utils.interaction.DropItemsInteraction;
import dslToGame.AnimationBuilder;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import starter.Game;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityFactory {

    /**
     * Create a new Entity that can be used as a playable character. It will have a {@link
     * PlayableComponent}. {@link PositionComponent}, {@link VelocityComponent} {@link
     * AnimationComponent}, {@link SkillComponent}, {@link HitboxComponent}.
     *
     * @return Created Entity
     */
    public static Entity getHero() {
        final int fireballCoolDown = 5;
        final float xSpeed = 0.3f;
        final float ySpeed = 0.3f;
        final String pathToIdleLeft = "knight/idleLeft";
        final String pathToIdleRight = "knight/idleRight";
        final String pathToRunLeft = "knight/runLeft";
        final String pathToRunRight = "knight/runRight";

        Entity hero = new Entity();
        new PositionComponent(hero);
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(hero, xSpeed, ySpeed, moveLeft, moveRight);
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(hero, idleLeft, idleRight);
        new HitboxComponent(
                hero,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
        PlayableComponent pc = new PlayableComponent(hero);
        Skill fireball =
                new Skill(
                        new FireballSkill(SkillTools::getCursorPositionAsPoint), fireballCoolDown);
        pc.setSkillSlot1(fireball);
        new SkillComponent(hero).addSkill(fireball);
        return hero;
    }

    /**
     * Create a new Entity that can be used as a chest.
     *
     * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
     * AnimationComponent}, {@link HitboxComponent} and {@link InventoryComponent}. It will use the
     * {@link DropItemsInteraction} on interaction.
     *
     * <p>{@link ItemDataGenerator} is used to generate random items
     *
     * @return Created Entity
     */
    public static Entity getChest() {
        Random random = new Random();
        ItemDataGenerator itemDataGenerator = new ItemDataGenerator();

        List<ItemData> itemData =
                IntStream.range(0, random.nextInt(1, 3))
                        .mapToObj(i -> itemDataGenerator.generateItemData())
                        .toList();
        return getChest(
                itemData,
                Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint());
    }

    /**
     * Create a new Entity that can be used as a chest.
     *
     * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
     * AnimationComponent}, {@link HitboxComponent} and {@link InventoryComponent}. It will use the
     * {@link DropItemsInteraction} on interaction.
     *
     * @param itemData The {@link ItemData} for the Items inside the chest.
     * @param position The position of the chest.
     * @return Created Entity
     */
    public static Entity getChest(List<ItemData> itemData, Point position) {
        final float defaultInteractionRadius = 1f;
        final List<String> DEFAULT_CLOSED_ANIMATION_FRAMES =
                List.of("objects/treasurechest/chest_full_open_anim_f0.png");
        final List<String> DEFAULT_OPENING_ANIMATION_FRAMES =
                List.of(
                        "objects/treasurechest/chest_full_open_anim_f0.png",
                        "objects/treasurechest/chest_full_open_anim_f1.png",
                        "objects/treasurechest/chest_full_open_anim_f2.png",
                        "objects/treasurechest/chest_empty_open_anim_f2.png");

        Entity chest = new Entity();
        new PositionComponent(chest, position);
        InventoryComponent ic = new InventoryComponent(chest, itemData.size());
        itemData.forEach(ic::addItem);
        new InteractionComponent(
                chest, defaultInteractionRadius, false, new DropItemsInteraction());
        new AnimationComponent(
                chest,
                new Animation(DEFAULT_CLOSED_ANIMATION_FRAMES, 100, false),
                new Animation(DEFAULT_OPENING_ANIMATION_FRAMES, 100, false));
        return chest;
    }
}
