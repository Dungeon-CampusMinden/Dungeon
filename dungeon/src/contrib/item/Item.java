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
import core.utils.logging.DungeonLogger;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;

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
public class Item implements CraftingIngredient, CraftingResult, Serializable {
  @Serial private static final long serialVersionUID = 1L;
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Item.class);

  /**
   * The maximum stack size for any item.
   *
   * <p>Cannot be higher than 255 due to byte storage.
   */
  private static final long MAX_STACK_SIZE = 64;

  /** Random object used to generate random numbers for item related things. */
  public static final Random RANDOM = new Random();

  protected static final String DATA_KEY_POTION_TYPE = "health_potion_type";
  protected static final String DATA_KEY_HEAL_AMOUNT = "heal_amount";
  private static final String DATA_KEY_PARAM_PREFIX = "param";

  static {
    ItemRegistry.register(ItemPotionHealth.class, Item::createHealthPotionFromData);
    ItemRegistry.register(ItemPotionWater.class);
    ItemRegistry.register(ItemResourceBerry.class);
    ItemRegistry.register(ItemResourceEgg.class);
    ItemRegistry.register(ItemResourceMushroomRed.class);
    ItemRegistry.register(ItemWoodenArrow.class);
    ItemRegistry.register(ItemWoodenBow.class);
    ItemRegistry.register(ItemBigKey.class);
    ItemRegistry.register(ItemFairy.class);
    ItemRegistry.register(ItemHammer.class);
    ItemRegistry.register(ItemHeart.class, Item::createHeartFromData);
    ItemRegistry.register(ItemKey.class);
  }

  private String displayName;
  private String description;
  private Animation inventoryAnimation;
  private Animation worldAnimation;
  private byte stackSize;
  private byte maxStackSize;

  /**
   * Determines whether the item uses simple interaction.
   *
   * <p>If set to {@code true}, interacting with the item will immediately pick it up. If set to
   * {@code false}, a more detailed interaction menu will be shown instead.
   */
  protected boolean simpleInteraction = true;

  /**
   * Create a new Item.
   *
   * @param displayName The display name of the item.
   * @param description The description of the item.
   * @param inventoryAnimation The inventory animation of the item.
   * @param worldAnimation The world animation of the item.
   * @param stackSize The stack size of the item (max 64).
   * @param maxStackSize The max stack size of the item (max 64).
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
    if (stackSize > MAX_STACK_SIZE || maxStackSize > MAX_STACK_SIZE) {
      throw new IllegalArgumentException(
          "Stack size and max stack size cannot be higher than " + MAX_STACK_SIZE);
    }
    this.stackSize = (byte) stackSize;
    this.maxStackSize = (byte) maxStackSize;

    // Stupidity check
    if (!Item.isRegistered(this.getClass())) {
      LOGGER.warn(
          "Item {} is not registered but instanced! Register class in Items!",
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
   * Returns item-specific data for network or persistence serialization.
   *
   * <p>Override this in subclasses that require constructor parameters to be reconstructed.
   *
   * @return item data map (empty by default)
   */
  public Map<String, String> itemData() {
    return Map.of();
  }

  /**
   * Register an item class using its simple name.
   *
   * @param clazz The class of the item to register.
   * @deprecated Use {@link ItemRegistry#register(Class)} instead.
   */
  @Deprecated
  public static void registerItem(final Class<? extends Item> clazz) {
    ItemRegistry.register(clazz);
  }

  /**
   * Get a copy of the registered items.
   *
   * <p>This method is used a copy of all registered items. It returns a map where the keys are the
   * simple names of the classes (e.g. {@link ItemResourceBerry}), and the values are the
   * corresponding class objects.
   *
   * @return A copy of the registered items.
   * @deprecated Use {@link ItemRegistry#entries()} instead.
   */
  @Deprecated
  public static Map<String, Class<? extends Item>> registeredItems() {
    return new HashMap<>(ItemRegistry.entries());
  }

  /**
   * Get an item by its identifier.
   *
   * <p>This method is used to get an item by its identifier. The identifier is the simple name of
   * the class (e.g. "ItemResourceBerry").
   *
   * @param id The identifier of the item.
   * @return The class of the item with the given identifier.
   * @deprecated Use {@link ItemRegistry#lookup(String)} instead.
   */
  @Deprecated
  public static Class<? extends Item> getItem(final String id) {
    return ItemRegistry.lookup(id).orElse(null);
  }

  private static boolean isRegistered(final Class<? extends Item> clazz) {
    return ItemRegistry.isRegistered(clazz);
  }

  static void ensureRegistryInitialized() {
    // Method intentionally empty; calling it triggers Item class initialization.
  }

  private static Item createHealthPotionFromData(Map<String, String> data) {
    Optional<HealthPotionType> type = resolveHealthPotionType(data);
    if (type.isEmpty()) {
      throw new IllegalArgumentException(
          "Missing or invalid item data for " + ItemPotionHealth.class.getSimpleName());
    }
    return new ItemPotionHealth(type.get());
  }

  private static Item createHeartFromData(Map<String, String> data) {
    OptionalInt healAmount = parseInt(data.get(DATA_KEY_HEAL_AMOUNT));
    if (healAmount.isEmpty()) {
      throw new IllegalArgumentException(
          "Missing or invalid item data for " + ItemHeart.class.getSimpleName());
    }
    return new ItemHeart(healAmount.getAsInt());
  }

  private static Optional<HealthPotionType> resolveHealthPotionType(Map<String, String> data) {
    String typeName = data.get(DATA_KEY_POTION_TYPE);
    if (typeName != null && !typeName.isBlank()) {
      try {
        return Optional.of(HealthPotionType.valueOf(typeName));
      } catch (IllegalArgumentException ignored) {
      }
    }

    for (Map.Entry<String, String> entry : data.entrySet()) {
      if (entry.getKey().startsWith(DATA_KEY_PARAM_PREFIX)) {
        Optional<HealthPotionType> parsed = parseHealthPotionType(entry.getValue());
        if (parsed.isPresent()) {
          return parsed;
        }
      }
    }

    OptionalInt healAmount = parseInt(data.get(DATA_KEY_HEAL_AMOUNT));
    if (healAmount.isPresent()) {
      return HealthPotionType.fromHealAmount(healAmount.getAsInt());
    }
    return Optional.empty();
  }

  private static Optional<HealthPotionType> parseHealthPotionType(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return Optional.empty();
    }
    String value = rawValue.strip();
    int separator = value.indexOf(':');
    if (separator >= 0 && separator + 1 < value.length()) {
      value = value.substring(separator + 1).strip();
    }
    try {
      return Optional.of(HealthPotionType.valueOf(value));
    } catch (IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }

  private static OptionalInt parseInt(String value) {
    if (value == null || value.isBlank()) {
      return OptionalInt.empty();
    }
    try {
      return OptionalInt.of(Integer.parseInt(value));
    } catch (NumberFormatException e) {
      return OptionalInt.empty();
    }
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
   * @param stackSize The new stack size (max {@link #MAX_STACK_SIZE}; min 0).
   */
  public void stackSize(int stackSize) {
    if (stackSize > MAX_STACK_SIZE || stackSize < 0) {
      throw new IllegalArgumentException(
          "Stack size cannot be higher than " + MAX_STACK_SIZE + " or lower than 0");
    }

    this.stackSize = (byte) stackSize;
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
   * @param maxStackSize The new max stack size (max {@link #MAX_STACK_SIZE}; min 1).
   */
  public void maxStackSize(int maxStackSize) {
    if (maxStackSize > MAX_STACK_SIZE || maxStackSize < 1) {
      throw new IllegalArgumentException(
          "Max stack size cannot be higher than " + MAX_STACK_SIZE + " or lower than 1");
    }
    this.maxStackSize = (byte) maxStackSize;
  }

  /**
   * Drops an item at the specified position and adds the created entity to the game.
   *
   * <p>If the {@link #simpleInteraction} flag is true, the item will be picked up on interaction.
   * If the flag is false, the more complex Interaction Menu will be shown.
   *
   * @param position the position where the item should be dropped
   * @return an {@code Optional} containing the dropped item entity if the drop was successful, or
   *     an empty {@code Optional} otherwise
   */
  public Optional<Entity> drop(final Point position) {
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof FloorTile) {
      Entity item;
      if (simpleInteraction)
        item = (WorldItemBuilder.buildWorldItemSimpleInteraction(this, position));
      else item = WorldItemBuilder.buildWorldItem(this, position);

      Game.add(item);
      return Optional.of(item);
    }
    return Optional.empty();
  }

  /**
   * Called when an item should be collected.
   *
   * @param itemEntity The entity that represents the item in the world.
   * @param collector The entity who collects the item. (Most likely the player)
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
  public void setAmount(int count) {
    this.stackSize(count);
  }

  @Override
  public int getAmount() {
    return stackSize;
  }

  @Override
  public CraftingType resultType() {
    return CraftingType.ITEM;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Item other = (Item) obj;

    return displayName.equals(other.displayName)
        && description.equals(other.description)
        && stackSize == other.stackSize
        && maxStackSize == other.maxStackSize;
  }
}
