package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.hud.GUICombination;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.utils.components.draw.ChestAnimations;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.interaction.InteractionTool;
import contrib.utils.components.item.ItemDataGenerator;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;

import core.Entity;
import core.Game;
import core.components.*;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;

import java.io.IOException;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityFactory {
    public static final int DEFAULT_INVENTORY_SIZE = 10;
    private static final Random RANDOM = new Random();
    private static final String HERO_FILE_PATH = "character/wizard";
    private static final float X_SPEED_HERO = 7.5f;
    private static final float Y_SPEED_HERO = 7.5f;
    private static final int FIREBALL_COOL_DOWN = 500;
    private static final String[] MONSTER_FILE_PATHS = {
        "character/monster/chort",
        "character/monster/imp",
        "character/monster/big_deamon",
        "character/monster/big_zombie",
        "character/monster/doc",
        "character/monster/goblin",
        "character/monster/ice_zombie",
        "character/monster/ogre",
        "character/monster/orc_shaman",
        "character/monster/orc_warrior",
        "character/monster/pumpkin_dude",
        "character/monster/zombie"
    };
    private static final int HERO_HP = 100;
    private static final int MIN_MONSTER_HEALTH = 10;

    // NOTE: +1 for health as nextInt() is exclusive
    private static final int MAX_MONSTER_HEALTH = 50 + 1;
    private static final float MIN_MONSTER_SPEED = 5.0f;
    private static final float MAX_MONSTER_SPEED = 8.5f;

    private static final DamageType MONSTER_COLLIDE_DAMAGE_TYPE = DamageType.PHYSICAL;
    private static final int MONSTER_COLLIDE_DAMAGE = 10;
    private static final int MONSTER_COLLIDE_COOL_DOWN = 2 * Game.frameRate();

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
        CameraComponent cc = new CameraComponent();
        hero.addComponent(cc);
        PositionComponent poc = new PositionComponent();
        hero.addComponent(poc);
        hero.addComponent(new VelocityComponent(X_SPEED_HERO, Y_SPEED_HERO));
        hero.addComponent(new DrawComponent(HERO_FILE_PATH));
        HealthComponent hc =
                new HealthComponent(
                        HERO_HP,
                        entity -> {
                            // play sound
                            Sound sound =
                                    Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav"));
                            long soundId = sound.play();
                            sound.setLooping(soundId, false);
                            sound.setVolume(soundId, 0.3f);
                            sound.setLooping(soundId, false);
                            sound.play();
                            sound.setVolume(soundId, 0.9f);

                            // relink components for camera
                            Entity cameraDummy = new Entity();
                            cameraDummy.addComponent(cc);
                            cameraDummy.addComponent(poc);
                            Game.add(cameraDummy);
                        });
        hero.addComponent(hc);
        hero.addComponent(
                new CollideComponent(
                        (you, other, direction) ->
                                other.fetch(SpikyComponent.class)
                                        .ifPresent(
                                                spikyComponent -> {
                                                    if (spikyComponent.isActive()) {
                                                        hc.receiveHit(
                                                                new Damage(
                                                                        spikyComponent
                                                                                .damageAmount(),
                                                                        spikyComponent.damageType(),
                                                                        other));
                                                        spikyComponent.activateCoolDown();
                                                    }
                                                }),
                        (you, other, direction) -> {}));

        hero.addComponent(new XPComponent((e) -> {}));
        PlayerComponent pc = new PlayerComponent();
        hero.addComponent(pc);
        InventoryComponent ic = new InventoryComponent(DEFAULT_INVENTORY_SIZE);
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
                            InventoryGUI.inHeroInventory = false;
                            e.removeComponent(UIComponent.class);
                        }
                    } else {
                        InventoryGUI.inHeroInventory = true;
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
                                                                    .orElseThrow(
                                                                            () ->
                                                                                    MissingComponentException
                                                                                            .build(
                                                                                                    x,
                                                                                                    UIComponent
                                                                                                            .class)))) // create a tuple to
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
                        InventoryGUI.inHeroInventory = false;
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

        Set<Item> items =
                IntStream.range(0, RANDOM.nextInt(1, 3))
                        .mapToObj(i -> itemDataGenerator.generateItemData())
                        .collect(Collectors.toSet());
        return newChest(items, PositionComponent.ILLEGAL_POSITION);
    }

    /**
     * Create a new Entity that can be used as a chest.
     *
     * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
     * DrawComponent}, {@link CollideComponent} and {@link InventoryComponent}. It will use the
     * {@link DropItemsInteraction} on interaction.
     *
     * @param item The {@link Item} for the Items inside the chest.
     * @param position The position of the chest.
     * @return Created Entity
     */
    public static Entity newChest(Set<Item> item, Point position) throws IOException {
        final float defaultInteractionRadius = 1f;
        Entity chest = new Entity("chest");

        if (position == null) chest.addComponent(new PositionComponent());
        else chest.addComponent(new PositionComponent(position));
        InventoryComponent ic = new InventoryComponent(item.size());
        chest.addComponent(ic);
        item.forEach(ic::add);
        chest.addComponent(
                new InteractionComponent(
                        defaultInteractionRadius,
                        true,
                        (interacted, interactor) -> {
                            interactor
                                    .fetch(InventoryComponent.class)
                                    .ifPresent(
                                            whoIc -> {
                                                UIComponent uiComponent =
                                                        new UIComponent(
                                                                new GUICombination(
                                                                        new InventoryGUI(whoIc),
                                                                        new InventoryGUI(ic)),
                                                                true);
                                                uiComponent.onClose(
                                                        () -> {
                                                            interacted
                                                                    .fetch(DrawComponent.class)
                                                                    .ifPresent(
                                                                            interactedDC -> {
                                                                                // remove all prior
                                                                                // opened animations
                                                                                interactedDC
                                                                                        .deQueueByPriority(
                                                                                                ChestAnimations
                                                                                                        .OPEN_FULL
                                                                                                        .priority());
                                                                                if (ic.count()
                                                                                        > 0) {
                                                                                    // aslong as
                                                                                    // there is an
                                                                                    // item inside
                                                                                    // the chest
                                                                                    // show a full
                                                                                    // chest
                                                                                    interactedDC
                                                                                            .queueAnimation(
                                                                                                    ChestAnimations
                                                                                                            .OPEN_FULL);
                                                                                } else {
                                                                                    // empty chest
                                                                                    // show the
                                                                                    // empty
                                                                                    // animation
                                                                                    interactedDC
                                                                                            .queueAnimation(
                                                                                                    ChestAnimations
                                                                                                            .OPEN_EMPTY);
                                                                                }
                                                                            });
                                                        });
                                                interactor.addComponent(uiComponent);
                                            });
                            interacted
                                    .fetch(DrawComponent.class)
                                    .ifPresent(
                                            interactedDC -> {
                                                // only add opening animation when it is not
                                                // finished
                                                if (interactedDC
                                                        .getAnimation(ChestAnimations.OPENING)
                                                        .map(animation -> !animation.isFinished())
                                                        .orElse(true)) {
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPENING);
                                                }
                                            });
                        }));
        DrawComponent dc = new DrawComponent("objects/treasurechest");
        var mapping = dc.animationMap();
        // set the closed chest as default idle
        mapping.put(
                CoreAnimations.IDLE.pathString(), mapping.get(ChestAnimations.CLOSED.pathString()));
        // opening animation should not loop
        mapping.get(ChestAnimations.OPENING.pathString()).setLoop(false);
        // reset Idle Animation
        dc.deQueueByPriority(CoreAnimations.IDLE.priority());
        dc.currentAnimation(CoreAnimations.IDLE);
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
                        (entity, who) ->
                                who.fetch(InventoryComponent.class)
                                        .ifPresent(
                                                ic -> {
                                                    CraftingGUI craftingGUI = new CraftingGUI(ic);
                                                    UIComponent component =
                                                            new UIComponent(
                                                                    new GUICombination(
                                                                            new InventoryGUI(ic),
                                                                            craftingGUI),
                                                                    true);
                                                    component.onClose(craftingGUI::cancel);
                                                    who.addComponent(component);
                                                })));
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
            Item item = itemDataGenerator.generateItemData();
            InventoryComponent ic = new InventoryComponent(1);
            monster.addComponent(ic);
            ic.add(item);
            onDeath =
                    (e, who) -> {
                        playMonsterDieSound();
                        new DropItemsInteraction().accept(e, who);
                    };
        } else {
            onDeath = (e, who) -> playMonsterDieSound();
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
        monster.addComponent(
                new SpikyComponent(
                        MONSTER_COLLIDE_DAMAGE,
                        MONSTER_COLLIDE_DAMAGE_TYPE,
                        MONSTER_COLLIDE_COOL_DOWN));
        monster.addComponent(new IdleSoundComponent(randomMonsterIdleSound()));
        return monster;
    }

    private static void playMonsterDieSound() {
        Sound dieSoundEffect;
        switch (RANDOM.nextInt(4)) {
            case 0 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_01.wav"));
            case 1 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_02.wav"));
            case 2 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_03.wav"));
            default -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_04.wav"));
        }
        long soundid = dieSoundEffect.play();
        dieSoundEffect.setLooping(soundid, false);
        dieSoundEffect.setVolume(soundid, 0.35f);
    }

    private static String randomMonsterIdleSound() {
        switch (RANDOM.nextInt(4)) {
            case 0 -> {
                return "sounds/monster1.wav";
            }
            case 1 -> {
                return "sounds/monster2.wav";
            }
            case 2 -> {
                return "sounds/monster3.wav";
            }
            default -> {
                return "sounds/monster4.wav";
            }
        }
    }
}
