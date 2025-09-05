package contrib.item;

import contrib.components.InventoryComponent;
import contrib.crafting.CraftingIngredient;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.entities.WorldItemBuilder;
import contrib.item.concreteItem.*;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.elements.tile.FloorTile;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.logging.CustomLogLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

  /** Random object used to generate random numbers for item related things. */
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
  private static final Map<String, Class<? extends Item>> REGISTERED_ITEMS = new HashMap<>();

  static {
    registerItem(ItemDefault.class);
    registerItem(ItemPotionHealth.class);
    registerItem(ItemPotionWater.class);
    registerItem(ItemResourceBerry.class);
    registerItem(ItemResourceEgg.class);
    registerItem(ItemResourceMushroomRed.class);
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
   * Register an item. This is used to associate the simple name of the class with the class object
   * in the {@link #REGISTERED_ITEMS} map.
   *
   * <p>When a new item is created, it should be registered using this method.
   *
   * @param clazz The class of the item to register.
   */
  public static void registerItem(final Class<? extends Item> clazz) {
    REGISTERED_ITEMS.put(clazz.getSimpleName(), clazz);
  }

  /**
   * Get a copy of the registered items.
   *
   * <p>This method is used a copy of all registered items. It returns a map where the keys are the
   * simple names of the classes (e.g. {@link ItemResourceBerry}), and the values are the
   * corresponding class objects.
   *
   * @return A copy of the registered items.
   */
  public static Map<String, Class<? extends Item>> registeredItems() {
    return new HashMap<>(REGISTERED_ITEMS);
  }

  /**
   * Get an item by its identifier.
   *
   * <p>This method is used to get an item by its identifier. The identifier is the simple name of
   * the class (e.g. "ItemResourceBerry").
   *
   * @param id The identifier of the item.
   * @return The class of the item with the given identifier.
   */
  public static Class<? extends Item> getItem(final String id) {
    return REGISTERED_ITEMS.get(id);
  }

  private static boolean isRegistered(final Class<? extends Item> clazz) {
    return REGISTERED_ITEMS.containsValue(clazz);
  }

  /**
   * Gets the display name of this item.
   *
   * <p>If there is more than one item in the stack, a prefix in the format "Count x DisplayName" is
   * returned.
   *
   * @return The display name.
   */
  public String displayName() {
    String prefix = "";
    if (this.stackSize > 1) prefix = this.stackSize() + " x ";
    return prefix.concat(this.displayName);
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
   * Drops an item at the specified position and adds the created entity to the game.
   *
   * @param position the position where the item should be dropped
   * @return an {@code Optional} containing the dropped item entity if the drop was successful, or
   *     an empty {@code Optional} otherwise
   */
  public Optional<Entity> drop(final Point position) {
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof FloorTile) {
      Entity item = (WorldItemBuilder.buildWorldItem(this, position));
      Game.add(item);
      return Optional.of(item);
    }
    return Optional.empty();
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
