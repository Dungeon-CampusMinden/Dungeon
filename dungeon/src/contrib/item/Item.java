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
import core.utils.components.draw.Animation;
import core.utils.logging.CustomLogLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Abstract class that represents every item in the game.
 *
 * <p>Every item has a display name, a description, an inventory animation and a world animation.
 *
 * <p>Items are droppable and collectable by default. Dropping an item will create a new {@link
 * Entity} in the world with the {@link contrib.components.ItemComponent} attached. Collecting an
 * item will add it to the inventory of the collector. To change this behavior, override the {@link
 * #drop(Point)} and {@link #collect(Entity, Entity)}.
 *
 * <p>Items have a max stack size and a stack size. The stack size is the amount of items on this
 * stack. The max stack size is the maximum amount of items that can be on this stack. The default
 * max stack size is 1 and the default stack size is 1. The values can be changed via the {@link
 * #stackSize()} and {@link #maxStackSize()} methods.
 */
public class Item implements CraftingIngredient, CraftingResult {
  private static final Logger LOGGER = Logger.getLogger(Item.class.getSimpleName());
  public static final Random RANDOM = new Random();

  /**
   * Maps identifiers in crafting recipes (e.g. {@link ItemResourceBerry}) to their corresponding
   * class objects (e.g. ItemResourceBerry.java). This map is used to associate identifiers in
   * crafting recipes with the actual classes and create an instance of the respective item when the
   * recipe is crafted.
   *
   * <p>The keys in the map are the simple names of the classes (e.g. "ItemBookRed"), and the values
   * are the corresponding class objects.
   */
  public static final Map<String, Class<? extends Item>> ITEMS = new HashMap<>();

  static {
    ITEMS.put(ItemDefault.class.getSimpleName(), ItemDefault.class);
    ITEMS.put(ItemPotionHealth.class.getSimpleName(), ItemPotionHealth.class);
    ITEMS.put(ItemPotionWater.class.getSimpleName(), ItemPotionWater.class);
    ITEMS.put(ItemResourceBerry.class.getSimpleName(), ItemResourceBerry.class);
    ITEMS.put(ItemResourceEgg.class.getSimpleName(), ItemResourceEgg.class);
    ITEMS.put(ItemResourceMushroomRed.class.getSimpleName(), ItemResourceMushroomRed.class);
  }

  private String displayName;
  private String description;
  private Animation inventoryAnimation;
  private Animation worldAnimation;
  private int stackSize;
  private int maxStackSize;

  /**
   * Create a new Item.
   *
   * @param displayName The display name of the item.
   * @param description The description of the item.
   * @param inventoryAnimation The inventory animation of the item.
   * @param worldAnimation The world animation of the item.
   * @param stackSize The stack size of the item.
   * @param maxStackSize The max stack size of the item.
   */
  public Item(
      final String displayName,
      final String description,
      final Animation inventoryAnimation,
      final Animation worldAnimation,
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
   * @param displayName The display name of the item.
   * @param description The description of the item.
   * @param inventoryAnimation The inventory animation of the item.
   * @param worldAnimation The world animation of the item.
   */
  public Item(
      final String displayName,
      final String description,
      final Animation inventoryAnimation,
      final Animation worldAnimation) {
    this(displayName, description, inventoryAnimation, worldAnimation, 1, 1);
  }

  /**
   * Create a new Item.
   *
   * <p>The default max stack size is 1 and the default stack size is 1.
   *
   * <p>The given animation is used for both the inventory and the world animation.
   *
   * @param displayName The display name.
   * @param description The description.
   * @param animation The animation.
   */
  public Item(final String displayName, final String description, final Animation animation) {
    this(displayName, description, animation, animation, 1, 1);
  }

  /**
   * Get the class name of the specific item.
   *
   * @param id WTF? .
   * @return The class name of the specific item.
   */
  public static Class<? extends Item> getItem(final String id) {
    return ITEMS.get(id);
  }

  protected static boolean isRegistered(final Class<? extends Item> clazz) {
    return ITEMS.containsValue(clazz);
  }

  /**
   * Get the display name of this item.
   *
   * @return The display name.
   */
  public String displayName() {
    return this.displayName;
  }

  /**
   * Set the display name of this item.
   *
   * @param displayName The new display name.
   */
  public void displayName(final String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get the description of this item.
   *
   * @return The description.
   */
  public String description() {
    return this.description;
  }

  /**
   * Set the description of this item.
   *
   * @param description The new description.
   */
  public void description(final String description) {
    this.description = description;
  }

  /**
   * Get the inventory animation of this item.
   *
   * @return The inventory animation.
   */
  public Animation inventoryAnimation() {
    return this.inventoryAnimation;
  }

  /**
   * Set the inventory animation of this item.
   *
   * @param inventoryAnimation The new inventory animation.
   */
  public void inventoryAnimation(final Animation inventoryAnimation) {
    this.inventoryAnimation = inventoryAnimation;
  }

  /**
   * Get the world animation of this item.
   *
   * @return The world animation.
   */
  public Animation worldAnimation() {
    return this.worldAnimation;
  }

  /**
   * Set the world animation of this item.
   *
   * @param worldAnimation The new world animation.
   */
  public void worldAnimation(final Animation worldAnimation) {
    this.worldAnimation = worldAnimation;
  }

  /**
   * Get the stack size of this item.
   *
   * @return The stack size.
   */
  public int stackSize() {
    return this.stackSize;
  }

  /**
   * Set the stack size of this item.
   *
   * @param stackSize The new stack size.
   */
  public void stackSize(int stackSize) {
    this.stackSize = stackSize;
  }

  /**
   * Get the max stack size of this item.
   *
   * @return The max stack size.
   */
  public int maxStackSize() {
    return this.maxStackSize;
  }

  /**
   * Set the max stack size of this item.
   *
   * @param maxStackSize The new max stack size.
   */
  public void maxStackSize(int maxStackSize) {
    this.maxStackSize = maxStackSize;
  }

  /**
   * Called when an item should be dropped.
   *
   * @param position The position where the item should be dropped.
   * @return Whether the item was dropped successfully.
   */
  public boolean drop(final Point position) {
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
  public boolean collect(final Entity itemEntity, final Entity collector) {
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
  public void use(final Entity user) {
    user.fetch(InventoryComponent.class).ifPresent(component -> component.remove(this));
  }

  @Override
  public boolean match(final CraftingIngredient input) {
    if (this.getClass().isInstance(input)) return ((Item) input).stackSize() <= stackSize;
    return false;
  }

  @Override
  public CraftingType resultType() {
    return CraftingType.ITEM;
  }
}
