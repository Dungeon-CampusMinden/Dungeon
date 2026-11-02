package feature.inventory;

import engine.Entity;
import engine.Game;
import engine.language.Localization;
import engine.level.Tile;
import engine.level.elements.tile.FloorTile;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.entities.WorldItemBuilder;
import feature.inventory.items.HintItem;
import feature.inventory.items.ItemHammer;
import feature.inventory.items.ItemKey;
import feature.inventory.items.ItemPotionHealth;
import feature.inventory.items.ItemWoodenArrow;
import feature.inventory.items.ItemWoodenBow;
import feature.tasks.AnswerItem;
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
 * Entity} in the world with the {@link feature.components.ItemComponent} attached. Collecting an
 * item will add it to the inventory of the collector. To change this behavior, override the {@link
 * #drop(Point)} and {@link #collect(Entity, Entity)}.
 *
 * <p>Items hold no mutable item state, so one instance can appear in several places (start items,
 * inventories, snapshots). Each inventory slot holds exactly one item; items do not stack.
 */
public class Item {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Item.class);

  /** Random object used to generate random numbers for item related things. */
  public static final Random RANDOM = new Random();

  protected static final String DATA_KEY_POTION_TYPE = "health_potion_type";
  protected static final String DATA_KEY_HEAL_AMOUNT = "heal_amount";
  private static final String DATA_KEY_PARAM_PREFIX = "param";

  static {
    ItemRegistry.register(ItemPotionHealth.class, Item::createHealthPotionFromData);
    ItemRegistry.register(ItemWoodenArrow.class);
    ItemRegistry.register(ItemWoodenBow.class);
    ItemRegistry.register(ItemHammer.class);
    ItemRegistry.register(ItemKey.class);
    HintItem.ensureRegistration();
    AnswerItem.ensureRegistration();
  }

  private final String displayName;
  private final String description;
  private final Animation inventoryAnimation;
  private final Animation worldAnimation;

  /**
   * Create a new Item.
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
    this.displayName = displayName;
    this.description = description;
    this.inventoryAnimation = inventoryAnimation;
    this.worldAnimation = worldAnimation;

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
   * <p>The given animation is used for both the inventory and the world animation.
   *
   * @param displayName The display name.
   * @param description The description.
   * @param animation The animation.
   */
  public Item(final String displayName, final String description, final Animation animation) {
    this(displayName, description, animation, animation);
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
   * Gets the translated display name of this item.
   *
   * @return The display name.
   */
  public String displayName() {
    return Localization.getInstance().getCurrentTranslator().translate(this.displayName);
  }

  /**
   * Get the description of this item.
   *
   * @return The description.
   */
  public String description() {
    return Localization.getInstance().getCurrentTranslator().translate(this.description);
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
   * Get the world animation of this item.
   *
   * @return The world animation.
   */
  public Animation worldAnimation() {
    return this.worldAnimation;
  }

  /**
   * Drops an item at the specified position and adds the created entity to the game.
   *
   * <p>Dropped items are picked up directly on interaction.
   *
   * @param position the position where the item should be dropped
   * @return an {@code Optional} containing the dropped item entity if the drop was successful, or
   *     an empty {@code Optional} otherwise
   */
  public Optional<Entity> drop(final Point position) {
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof FloorTile) {
      Entity item = WorldItemBuilder.buildWorldItem(this, position);

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
}
