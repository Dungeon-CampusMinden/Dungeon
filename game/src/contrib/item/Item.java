package contrib.item;

import contrib.components.InventoryComponent;
import contrib.crafting.CraftingIngredient;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.entities.WorldItemBuilder;
import contrib.item.concreteItem.*;

import core.Entity;
import core.Game;
import core.level.elements.tile.FloorTile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.logging.CustomLogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Abstract class that represents every item in the game.
 *
 * <p>Every item has a display name, a description, an inventory animation and a world animation.
 *
 * <p>Items are droppable and collectable by default. Dropping an item will create a new {@link
 * Entity} in the world with the {@link contrib.components.ItemComponent} attached. Collecting an
 * item will add it to the inventory of the collector. To change this behavior, override the {@link
 * #drop(Entity, Point)} and {@link #collect(Entity, Entity)}.
 *
 * <p>Items have a max stack size and a stack size. The stack size is the amount of items on this
 * stack. The max stack size is the maximum amount of items that can be on this stack. The default
 * max stack size is 1 and the default stack size is 1. The values can be changed via the {@link
 * #stackSize()} and {@link #maxStackSize()} methods.
 */
public class Item implements CraftingIngredient, CraftingResult {
    private static final Logger LOGGER = Logger.getLogger(Item.class.getName());

    /**
     * Maps identifiers in crafting recipes (e.g. {@link ItemResourceBerry}) to their corresponding
     * class objects (e.g. ItemResourceBerry.java). This map is used to associate identifiers in
     * crafting recipes with the actual classes and create an instance of the respective item when
     * the recipe is crafted.
     *
     * <p>The keys in the map are the simple names of the classes (e.g. "ItemBookRed"), and the
     * values are the corresponding class objects.
     */
    private static final Map<String, Class<? extends Item>> items = new HashMap<>();

    static {
        items.put(ItemDefault.class.getSimpleName(), ItemDefault.class);
        items.put(ItemPotionHealth.class.getSimpleName(), ItemPotionHealth.class);
        items.put(ItemPotionWater.class.getSimpleName(), ItemPotionWater.class);
        items.put(ItemResourceBerry.class.getSimpleName(), ItemResourceBerry.class);
        items.put(ItemResourceEgg.class.getSimpleName(), ItemResourceEgg.class);
        items.put(ItemResourceMushroomRed.class.getSimpleName(), ItemResourceMushroomRed.class);
    }

    private String displayName;
    private String description;
    private Animation inventoryAnimation;
    private Animation worldAnimation;
    private int stackSize = 1;
    private int maxStackSize = 1;

    /**
     * Create a new Item.
     *
     * @param displayName the display name of the item
     * @param description the description of the item
     * @param inventoryAnimation the inventory animation of the item
     * @param worldAnimation the world animation of the item
     * @param stackSize the stack size of the item
     * @param maxStackSize the max stack size of the item
     */
    public Item(
            String displayName,
            String description,
            Animation inventoryAnimation,
            Animation worldAnimation,
            int stackSize,
            int maxStackSize) {
        this.displayName = displayName;
        this.description = description;
        this.inventoryAnimation = inventoryAnimation;
        this.worldAnimation = worldAnimation;
        this.stackSize = stackSize;
        this.maxStackSize = maxStackSize;

        // Stupidity check
        if (!Item.isRegistered(this.getClass())) {
            LOGGER.log(
                    CustomLogLevel.WARNING,
                    "Item {0} is not registered but instanced! Register class in Items!",
                    this.getClass().getName());
        }
    }

    /**
     * Create a new Item.
     *
     * <p>The default max stack size is 1 and the default stack size is 1.
     *
     * @param displayName the display name of the item
     * @param description the description of the item
     * @param inventoryAnimation the inventory animation of the item
     * @param worldAnimation the world animation of the item
     */
    public Item(
            String displayName,
            String description,
            Animation inventoryAnimation,
            Animation worldAnimation) {
        this(displayName, description, inventoryAnimation, worldAnimation, 1, 1);
    }

    /**
     * Create a new Item.
     *
     * <p>The default max stack size is 1 and the default stack size is 1.
     *
     * <p>The given animation is used for both the inventory and the world animation.
     *
     * @param displayName the display name
     * @param description the description
     * @param animation the animation
     */
    public Item(String displayName, String description, Animation animation) {
        this(displayName, description, animation, animation, 1, 1);
    }

    /**
     * Get the class name of the specific item.
     *
     * @return The class name of the specific item.
     */
    public static Class<? extends Item> getItem(String id) {
        return items.get(id);
    }

    protected static boolean isRegistered(Class<? extends Item> clazz) {
        return items.containsValue(clazz);
    }

    /**
     * Get the display name of this item.
     *
     * @return the display name.
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the display name of this item.
     *
     * @param displayName the new display name.
     */
    public void displayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the description of this item.
     *
     * @return the description.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description of this item.
     *
     * @param description the new description.
     */
    public void description(String description) {
        this.description = description;
    }

    /**
     * Get the inventory animation of this item.
     *
     * @return the inventory animation.
     */
    public Animation inventoryAnimation() {
        return this.inventoryAnimation;
    }

    /**
     * Set the inventory animation of this item.
     *
     * @param inventoryAnimation the new inventory animation.
     */
    public void inventoryAnimation(Animation inventoryAnimation) {
        this.inventoryAnimation = inventoryAnimation;
    }

    /**
     * Get the world animation of this item.
     *
     * @return the world animation.
     */
    public Animation worldAnimation() {
        return this.worldAnimation;
    }

    /**
     * Set the world animation of this item.
     *
     * @param worldAnimation the new world animation.
     */
    public void worldAnimation(Animation worldAnimation) {
        this.worldAnimation = worldAnimation;
    }

    /**
     * Get the stack size of this item.
     *
     * @return the stack size.
     */
    public int stackSize() {
        return this.stackSize;
    }

    /**
     * Set the stack size of this item.
     *
     * @param stackSize the new stack size.
     */
    public void stackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    /**
     * Get the max stack size of this item.
     *
     * @return the max stack size.
     */
    public int maxStackSize() {
        return this.maxStackSize;
    }

    /**
     * Set the max stack size of this item.
     *
     * @param maxStackSize the new max stack size.
     */
    public void maxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    /**
     * Called when an item should be dropped.
     *
     * <p>The dropper entity is unused in the default implementation but may be used in subclasses.
     *
     * @param dropper The entity who drops the item.
     * @param position The position where the item should be dropped.
     * @return Whether the item was dropped successfully.
     */
    public boolean drop(Entity dropper, Point position) {
        if (Game.tileAT(position) instanceof FloorTile) {
            Game.add(WorldItemBuilder.buildWorldItem(this, position));
            return true;
        }
        return false;
    }

    /**
     * Called when an item should be collected.
     *
     * @param itemEntity The entity that represents the item in the world.
     * @param collector The entity who collects the item. (Most likely the hero)
     * @return Whether the item was collected successfully.
     */
    public boolean collect(Entity itemEntity, Entity collector) {
        return collector
                .fetch(InventoryComponent.class)
                .map(
                        inventoryComponent -> {
                            if (inventoryComponent.add(this)) {
                                Game.remove(itemEntity);
                                return true;
                            }
                            return false;
                        })
                .orElse(false);
    }

    /**
     * Defines the behavior when an item gets used. Prints a message to the console and removes the
     * item from the inventory.
     *
     * @param user Entity that uses the item.
     */
    public void use(Entity user) {
        user.fetch(InventoryComponent.class).ifPresent(component -> component.remove(this));
    }

    @Override
    public CraftingType ingredientType() {
        return CraftingType.ITEM;
    }

    @Override
    public boolean match(CraftingIngredient input) {
        if (this.getClass().isInstance(input)) return ((Item) input).stackSize() <= stackSize;
        return false;
    }

    @Override
    public CraftingType resultType() {
        return CraftingType.ITEM;
    }

    @Override
    public void executeCrafting(Entity entity) {
        entity.fetch(InventoryComponent.class)
                .ifPresentOrElse(
                        inv -> inv.add(this),
                        () -> {
                            throw MissingComponentException.build(entity, InventoryComponent.class);
                        });
    }
}
