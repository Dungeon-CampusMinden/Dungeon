package contrib.entities;

import contrib.components.*;
import contrib.configuration.KeyboardConfig;
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
import core.utils.Constants;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityFactory {
    private static final Logger LOGGER = Logger.getLogger(EntityFactory.class.getName());
    private static final Random RANDOM = new Random();
    private static final String HERO_FILE_PATH = "character/knight";
    private static final float X_SPEED_HERO = 0.3f;
    private static final float Y_SPEED_HERO = 0.3f;
    private static final int FIREBALL_COOL_DOWN = 2;
    private static final String[] MONSTER_FILE_PATHS = {
        "character/monster/chort", "character/monster/imp"
    };

    private static final int MIN_MONSTER_HEALTH = 2;

    // NOTE: +1 for health as nextInt() is exclusive
    private static final int MAX_MONSTER_HEALTH = 5 + 1;
    private static final float MIN_MONSTER_SPEED = 0.1f;
    private static final float MAX_MONSTER_SPEED = 0.25f;

    /**
     * Create a new Entity that can be used as a playable character. It will have a {@link
     * CameraComponent}, {@link PlayerComponent}. {@link PositionComponent}, {@link
     * VelocityComponent} {@link DrawComponent}, {@link CollideComponent}, {@link HealthComponent}
     * and {@link XPComponent}.
     *
     * @return Created Entity
     */
    public static Entity newHero() throws IOException {
        Entity hero = new Entity("hero");
        new CameraComponent(hero);
        new PositionComponent(hero);
        new VelocityComponent(hero, X_SPEED_HERO, Y_SPEED_HERO);
        new DrawComponent(hero, HERO_FILE_PATH);
        new InventoryComponent(hero, Constants.DEFAULT_INVENTORY_SIZE);
        new CollideComponent(
                hero,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
        new HealthComponent(hero, 200, Game::remove);
        new XPComponent(hero, (e) -> {});
        PlayerComponent pc = new PlayerComponent(hero);
        Skill fireball =
                new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), FIREBALL_COOL_DOWN);

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
                InteractionTool::interactWithClosestInteractable,
                false);

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
        ItemDataGenerator itemDataGenerator = new ItemDataGenerator();

        Set<ItemData> itemData =
                IntStream.range(0, RANDOM.nextInt(1, 3))
                        .mapToObj(i -> itemDataGenerator.generateItemData())
                        .collect(Collectors.toSet());
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
    public static Entity newChest(Set<ItemData> itemData, Point position) throws IOException {
        final float defaultInteractionRadius = 1f;
        Entity chest = new Entity("chest");
        new PositionComponent(chest, position);
        InventoryComponent ic = new InventoryComponent(chest, itemData.size());
        itemData.forEach(ic::add);
        new InteractionComponent(
                chest, defaultInteractionRadius, false, new DropItemsInteraction());
        DrawComponent dc = new DrawComponent(chest, "objects/treasurechest");
        dc.getAnimation(CoreAnimations.IDLE_RIGHT).ifPresent(a -> a.setLoop(false));

        return chest;
    }

    /**
     * Create a new Entity that can be used as a Monster.
     *
     * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent}
     * with random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set
     * Animation, {@link VelocityComponent}, {@link CollideComponent} and a 10% chance for an {@link
     * InventoryComponent}. If it has an Inventory it will use the {@link DropItemsInteraction} on
     * death.
     *
     * @return The generated "Monster".
     */
    public static Entity randomMonster() throws IOException {
        return randomMonster(MONSTER_FILE_PATHS[RANDOM.nextInt(0, MONSTER_FILE_PATHS.length)]);
    }

    /**
     * Create a new Entity that can be used as a Monster.
     *
     * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent}
     * with random AIs from the {@link AIFactory} class, {@link DrawComponent} with the Animations
     * in the given path, {@link VelocityComponent}, {@link CollideComponent} and a 10% chance for
     * an {@link InventoryComponent}. If it has an Inventory it will use the {@link
     * DropItemsInteraction} on death.
     *
     * @param pathToTexture Path to the directory that contains the texture that should be used for
     *     the created monster
     * @return The generated "Monster".
     * @see DrawComponent
     */
    public static Entity randomMonster(String pathToTexture) throws IOException {
        int health = RANDOM.nextInt(MIN_MONSTER_HEALTH, MAX_MONSTER_HEALTH);
        float speed = RANDOM.nextFloat(MIN_MONSTER_SPEED, MAX_MONSTER_SPEED);

        Entity monster = new Entity("monster");
        int itemRoll = RANDOM.nextInt(0, 10);
        BiConsumer<Entity, Entity> onDeath;
        if (itemRoll == 0) {
            ItemDataGenerator itemDataGenerator = new ItemDataGenerator();
            ItemData item = itemDataGenerator.generateItemData();
            InventoryComponent ic = new InventoryComponent(monster, 1);
            ic.add(item);
            onDeath = new DropItemsInteraction();
        } else {
            onDeath = (e, who) -> {};
        }
        new HealthComponent(monster, health, (e) -> onDeath.accept(e, null));
        new PositionComponent(monster);
        new AIComponent(
                monster,
                AIFactory.generateRandomFightAI(),
                AIFactory.generateRandomIdleAI(),
                AIFactory.generateRandomTransitionAI(monster));
        new DrawComponent(monster, pathToTexture);
        new VelocityComponent(monster, speed, speed);
        new CollideComponent(monster);
        return monster;
    }
}
