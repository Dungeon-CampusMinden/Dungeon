package content.entity;

import api.Entity;
import api.Game;
import api.components.*;
import api.level.utils.LevelElement;
import api.utils.Point;
import api.utils.component_utils.animationComponent.Animation;
import api.utils.component_utils.itemComponent.ItemData;
import api.utils.component_utils.skillComponent.Skill;
import content.component.SkillComponent;
import content.utils.interactionComponent.DropItemsInteraction;
import content.utils.itemComponent.ItemDataGenerator;
import content.utils.skillComponent.FireballSkill;
import content.utils.skillComponent.SkillTools;
import dslToGame.AnimationBuilder;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityFactory {

    /**
     * Create a new Entity that can be used as a playable character. It will have a {@link
     * PlayerComponent}. {@link PositionComponent}, {@link VelocityComponent} {@link DrawComponent},
     * {@link SkillComponent}, {@link CollideComponent}.
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
        new DrawComponent(hero, idleLeft, idleRight);
        new CollideComponent(
                hero,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
        PlayerComponent pc = new PlayerComponent(hero);
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
     * DrawComponent}, {@link CollideComponent} and {@link InventoryComponent}. It will use the
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
     * DrawComponent}, {@link CollideComponent} and {@link InventoryComponent}. It will use the
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
        new DrawComponent(
                chest,
                new Animation(DEFAULT_CLOSED_ANIMATION_FRAMES, 100, false),
                new Animation(DEFAULT_OPENING_ANIMATION_FRAMES, 100, false));
        return chest;
    }
}
