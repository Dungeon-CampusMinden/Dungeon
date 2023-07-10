package contrib.entities;

import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.collision.DefaultCollider;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.interaction.InteractionTool;
import contrib.utils.components.item.ItemData;
import contrib.utils.components.item.ItemDataGenerator;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;

import core.Entity;
import core.Game;
import core.components.*;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityFactory {
    private static final Logger LOGGER = Logger.getLogger(EntityFactory.class.getName());

    /**
     * Create a new Entity that can be used as a playable character. It will have a {@link
     * CameraComponent}, {@link PlayerComponent}. {@link PositionComponent}, {@link
     * VelocityComponent} {@link DrawComponent}, {@link CollideComponent}.
     *
     * @return Created Entity
     */
    public static Entity newHero() throws IOException {
        final int fireballCoolDown = 2;
        final float xSpeed = 0.3f;
        final float ySpeed = 0.3f;

        Entity hero = new Entity("hero");
        new CameraComponent(hero);
        new PositionComponent(hero);
        new VelocityComponent(hero, xSpeed, ySpeed);
        new DrawComponent(hero, "character/knight");
        new CollideComponent(
                hero,
                new DefaultCollider("heroCollisionEnter"),
                new DefaultCollider("heroCollisionLeave"));
        PlayerComponent pc = new PlayerComponent(hero);
        Skill fireball =
                new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), fireballCoolDown);

        // hero movement
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_UP.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));
                    vc.currentYVelocity(1 * vc.yVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_DOWN.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentYVelocity(-1 * vc.yVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_RIGHT.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentXVelocity(1 * vc.xVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_LEFT.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentXVelocity(-1 * vc.xVelocity());
                });

        pc.registerCallback(
                KeyboardConfig.INTERACT_WORLD.value(),
                InteractionTool::interactWithClosestInteractable);

        // skills
        pc.registerCallback(KeyboardConfig.FIRST_SKILL.value(), fireball::execute);

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
    public static Entity newChest() throws IOException {
        Random random = new Random();
        ItemDataGenerator itemDataGenerator = new ItemDataGenerator();

        List<ItemData> itemData =
                IntStream.range(0, random.nextInt(1, 3))
                        .mapToObj(i -> itemDataGenerator.generateItemData())
                        .toList();
        return newChest(itemData, Game.randomTile(LevelElement.FLOOR).position());
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
    public static Entity newChest(List<ItemData> itemData, Point position) throws IOException {
        final float defaultInteractionRadius = 1f;
        Entity chest = new Entity("chest");
        new PositionComponent(chest, position);
        InventoryComponent ic = new InventoryComponent(chest, itemData.size());
        itemData.forEach(ic::addItem);
        new InteractionComponent(
                chest, defaultInteractionRadius, false, new DropItemsInteraction());
        DrawComponent dc = new DrawComponent(chest, "objects/treasurechest");
        dc.getAnimation(CoreAnimations.IDLE_RIGHT).ifPresent(a -> a.setLoop(false));

        return chest;
    }

    // Todo - Adjust to changes
    public static Entity newMonster() throws IOException {
        return newMonster(Game.randomTilePoint(LevelElement.FLOOR));
    }

    // Todo - Adjust to changes
    public static Entity newMonster(Point position) throws IOException {
        Entity monster = new Entity("chort");

        // Add components to the monster entity
        new PositionComponent(monster, position);
        new DrawComponent(monster, "character/monster/chort");
        new VelocityComponent(monster, 0.1f, 0.1f);
        new HealthComponent(monster);
        new CollideComponent(
                monster,
                new DefaultCollider("ChortCollisionEnter"),
                new DefaultCollider("ChortCollisionLeave"));
        new AIComponent(
                monster, new CollideAI(1.0f), new RadiusWalk(5, 1), new SelfDefendTransition());

        return monster;
    }
}
