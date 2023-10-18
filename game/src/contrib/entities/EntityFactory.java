package contrib.entities;

import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.hud.GUICombination;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.inventory.InventoryGUI;
import contrib.utils.components.draw.ChestAnimations;
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
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;

import java.io.IOException;
import java.util.Comparator;
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
    private static final float X_SPEED_HERO = 7.5f;
    private static final float Y_SPEED_HERO = 7.5f;
    private static final int FIREBALL_COOL_DOWN = 1;
    private static final String[] MONSTER_FILE_PATHS = {
        "character/monster/chort", "character/monster/imp"
    };

    private static final int MIN_MONSTER_HEALTH = 2;

    // NOTE: +1 for health as nextInt() is exclusive
    private static final int MAX_MONSTER_HEALTH = 5 + 1;
    private static final float MIN_MONSTER_SPEED = 3.0f;
    private static final float MAX_MONSTER_SPEED = 7.5f;

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
        hero.addComponent(new CameraComponent());
        hero.addComponent(new PositionComponent());
        hero.addComponent(new VelocityComponent(X_SPEED_HERO, Y_SPEED_HERO));
        hero.addComponent(new DrawComponent(HERO_FILE_PATH));
        hero.addComponent(
                new CollideComponent(
                        (you, other, direction) -> System.out.println("heroCollisionEnter"),
                        (you, other, direction) -> System.out.println("heroCollisionLeave")));
        hero.addComponent(new HealthComponent(200, Game::remove));
        hero.addComponent(new XPComponent((e) -> {}));
        PlayerComponent pc = new PlayerComponent();
        hero.addComponent(pc);
        InventoryComponent ic = new InventoryComponent(Constants.DEFAULT_INVENTORY_SIZE);
        hero.addComponent(ic);
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
                KeyboardConfig.INVENTORY_OPEN.value(),
                (e) -> {
                    UIComponent uiComponent = e.fetch(UIComponent.class).orElse(null);
                    if (uiComponent != null) {
                        if (uiComponent.dialog() instanceof GUICombination) {
                            e.removeComponent(UIComponent.class);
                        }
                    } else {
                        e.addComponent(
                                new UIComponent(new GUICombination(new InventoryGUI(ic)), true));
                    }
                },
                false,
                false);

        pc.registerCallback(
                KeyboardConfig.CLOSE_UI.value(),
                (e) -> {
                    var firstUI =
                            Game.entityStream() // would be nice to directly access HudSystems
                                    // stream (no access to the System object)
                                    .filter(
                                            x ->
                                                    x.isPresent(
                                                            UIComponent.class)) // find all Entities
                                    // which have a
                                    // UIComponent
                                    .map(
                                            x ->
                                                    new Tuple<>(
                                                            x,
                                                            x.fetch(UIComponent.class)
                                                                    .get())) // create a tuple to
                                    // still have access to
                                    // the UI Entity
                                    .filter(x -> x.b().closeOnUICloseKey())
                                    .max(
                                            Comparator.comparingInt(
                                                    x -> x.b().dialog().getZIndex())) // find dialog
                                    // with highest
                                    // zindex
                                    .orElse(null);
                    if (firstUI != null) {
                        firstUI.a().removeComponent(UIComponent.class);
                        if (firstUI.a().componentStream().findAny().isEmpty()) {
                            Game.remove(firstUI.a()); // delete unused Entity
                        }
                    }
                },
                false,
                false);

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
        if (Game.currentLevel() == null) return newChest(itemData, null);
        else return newChest(itemData, Game.randomTile(LevelElement.FLOOR).position());
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

        if (position == null) chest.addComponent(new PositionComponent());
        else chest.addComponent(new PositionComponent(position));
        InventoryComponent ic = new InventoryComponent(itemData.size());
        chest.addComponent(ic);
        itemData.forEach(ic::add);
        chest.addComponent(
                new InteractionComponent(
                        defaultInteractionRadius,
                        true,
                        (interacted, interactor) -> {
                            interactor
                                    .fetch(InventoryComponent.class)
                                    .ifPresent(
                                            whoIc -> {
                                                interactor.addComponent(
                                                        new UIComponent(
                                                                new GUICombination(
                                                                        new InventoryGUI(whoIc),
                                                                        new InventoryGUI(ic)),
                                                                false));
                                            });
                            interacted
                                    .fetch(DrawComponent.class)
                                    .ifPresent(
                                            interactedDC -> {
                                                // only add opening animation when it is not
                                                // finished
                                                if (interactedDC
                                                        .getAnimation(ChestAnimations.OPENING)
                                                        .map(Animation::isFinished)
                                                        .orElse(true)) {
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPENING);
                                                }
                                                // remove all prior opened animations
                                                interactedDC.deQueueByPriority(
                                                        ChestAnimations.OPEN_FULL.priority());
                                                if (ic.count() > 0) {
                                                    // aslong as there is an item inside the chest
                                                    // show a full chest
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPEN_FULL);
                                                } else {
                                                    // empty chest show the empty animation
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPEN_EMPTY);
                                                }
                                            });
                        }));
        DrawComponent dc = new DrawComponent("objects/treasurechest");
        var mapping = dc.animationMap();
        // set the closed chest as default idle
        mapping.put(
                CoreAnimations.IDLE.pathString(), mapping.get(ChestAnimations.CLOSED.pathString()));
        // make opening animation not looping
        mapping.get(ChestAnimations.OPENING.pathString()).setLoop(false);

        chest.addComponent(dc);

        return chest;
    }

    /**
     * Create a new Entity that can be used as a crafting cauldron.
     *
     * @return Created Entity
     * @throws IOException if the textures do not exist
     */
    public static Entity newCraftingCauldron() throws IOException {
        Entity cauldron = new Entity("cauldron");
        cauldron.addComponent(new PositionComponent());
        cauldron.addComponent(new DrawComponent("objects/cauldron"));
        cauldron.addComponent(new CollideComponent());
        cauldron.addComponent(
                new InteractionComponent(
                        1f,
                        true,
                        (entity, who) -> {
                            who.fetch(InventoryComponent.class)
                                    .ifPresent(
                                            ic ->
                                                    who.addComponent(
                                                            new UIComponent(
                                                                    new GUICombination(
                                                                            new InventoryGUI(ic),
                                                                            new CraftingGUI(ic)),
                                                                    true)));
                        }));
        return cauldron;
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
            InventoryComponent ic = new InventoryComponent(1);
            monster.addComponent(ic);
            ic.add(item);
            onDeath = new DropItemsInteraction();
        } else {
            onDeath = (e, who) -> {};
        }
        monster.addComponent(new HealthComponent(health, (e) -> onDeath.accept(e, null)));
        monster.addComponent(new PositionComponent());
        monster.addComponent(
                new AIComponent(
                        AIFactory.generateRandomFightAI(),
                        AIFactory.generateRandomIdleAI(),
                        AIFactory.generateRandomTransitionAI(monster)));
        monster.addComponent(new DrawComponent(pathToTexture));
        monster.addComponent(new VelocityComponent(speed, speed));
        monster.addComponent(new CollideComponent());
        return monster;
    }
}
