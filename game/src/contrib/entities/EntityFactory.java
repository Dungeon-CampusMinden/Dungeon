package contrib.entities;

import contrib.components.CollideComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
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
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.hud.Inventory.InventoryGUI;
import core.level.utils.LevelElement;
import core.utils.Point;
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
     * PlayerComponent}. {@link PositionComponent}, {@link VelocityComponent} {@link DrawComponent},
     * {@link CollideComponent}.
     *
     * @return Created Entity
     */
    public static Entity getHero() throws IOException {
        final int fireballCoolDown = 2;
        final float xSpeed = 0.3f;
        final float ySpeed = 0.3f;

        Entity hero = new Entity("hero");
        new PositionComponent(hero);
        new VelocityComponent(hero, xSpeed, ySpeed);
        new DrawComponent(hero, "character/knight");
        new CollideComponent(
                hero,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
        PlayerComponent pc = new PlayerComponent(hero);
        Skill fireball =
                new Skill(
                        new FireballSkill(SkillTools::getCursorPositionAsPoint), fireballCoolDown);

        // hero movement
        pc.registerFunction(
                KeyboardConfig.MOVEMENT_UP.get(),
                entity -> {
                    VelocityComponent vc =
                            (VelocityComponent) entity.getComponent(VelocityComponent.class).get();
                    vc.setCurrentYVelocity(1 * vc.getYVelocity());
                });
        pc.registerFunction(
                KeyboardConfig.MOVEMENT_DOWN.get(),
                entity -> {
                    VelocityComponent vc =
                            (VelocityComponent) entity.getComponent(VelocityComponent.class).get();
                    vc.setCurrentYVelocity(-1 * vc.getYVelocity());
                });
        pc.registerFunction(
                KeyboardConfig.MOVEMENT_RIGHT.get(),
                entity -> {
                    VelocityComponent vc =
                            (VelocityComponent) entity.getComponent(VelocityComponent.class).get();
                    vc.setCurrentXVelocity(1 * vc.getXVelocity());
                });
        pc.registerFunction(
                KeyboardConfig.MOVEMENT_LEFT.get(),
                entity -> {
                    VelocityComponent vc =
                            (VelocityComponent) entity.getComponent(VelocityComponent.class).get();
                    vc.setCurrentXVelocity(-1 * vc.getXVelocity());
                });

        pc.registerFunction(
                KeyboardConfig.INTERACT_WORLD.get(),
                InteractionTool::interactWithClosestInteractable);

        // skills
        pc.registerFunction(KeyboardConfig.FIRST_SKILL.get(), fireball::execute);

        pc.registerFunction(
                KeyboardConfig.INVENTORY_OPEN.get(),
                entity -> {
                    InventoryGUI inventoryGUI = InventoryGUI.getInstance();
                    if (inventoryGUI.isOpen()) {
                        inventoryGUI.closeInventory();
                    } else {
                        inventoryGUI.openInventory();
                    }
                });

        new InventoryComponent(hero, 10);

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
    public static Entity getChest() throws IOException {
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
    public static Entity getChest(List<ItemData> itemData, Point position) throws IOException {
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
}
