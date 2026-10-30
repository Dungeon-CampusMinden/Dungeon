package feature.prefabs;

import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A kind of item passed between prefabs as an artifact, e.g. the picture granted for completing an
 * image puzzle.
 *
 * <p>An item artifact describes items rather than being one item object: in multiplayer every
 * player can hold their own copy. Items match the artifact if they have the same registry ID and
 * the same item data, which also survives network transfers.
 *
 * @param itemId registry ID of the item class
 * @param itemData item data identifying the item, see {@link Item#itemData()}
 * @param displayName name shown to players and in dialog placeholders
 */
public record ItemArtifact(String itemId, Map<String, String> itemData, String displayName) {

  /** Validates the members and copies the item data. */
  public ItemArtifact {
    Objects.requireNonNull(itemId, "itemId");
    itemData = Map.copyOf(Objects.requireNonNull(itemData, "itemData"));
    Objects.requireNonNull(displayName, "displayName");
  }

  /**
   * Describes the kind of the given item.
   *
   * @param item example item
   * @return artifact matching items equal to the example
   */
  public static ItemArtifact of(Item item) {
    return new ItemArtifact(ItemRegistry.idFor(item), item.itemData(), item.displayName());
  }

  /**
   * Checks whether an item is of this kind.
   *
   * @param item item to check
   * @return true if the item has the same registry ID and item data
   */
  public boolean matches(Item item) {
    if (item == null) return false;
    Optional<String> id = ItemRegistry.idFor(item.getClass());
    return id.isPresent() && id.get().equals(itemId) && item.itemData().equals(itemData);
  }

  /**
   * Creates a new item of this kind.
   *
   * <p>Items without item data are created with the no-argument constructor of their class if it
   * has one, other items with their registered factory.
   *
   * @return the new item, or empty if the item can not be created
   */
  public Optional<Item> create() {
    if (itemData.isEmpty()) {
      Optional<Item> item = ItemRegistry.lookup(itemId).flatMap(ItemArtifact::instantiate);
      if (item.isPresent()) return item;
    }
    return ItemRegistry.create(itemId, itemData);
  }

  /**
   * Returns whether items of a class can be created without item data.
   *
   * @param itemClass item class
   * @return true if the class has a public no-argument constructor
   */
  public static boolean isInstantiable(Class<? extends Item> itemClass) {
    if (Modifier.isAbstract(itemClass.getModifiers())) return false;
    try {
      itemClass.getConstructor();
      return true;
    } catch (NoSuchMethodException exception) {
      return false;
    }
  }

  private static Optional<Item> instantiate(Class<? extends Item> itemClass) {
    if (!isInstantiable(itemClass)) return Optional.empty();
    try {
      return Optional.of(itemClass.getConstructor().newInstance());
    } catch (ReflectiveOperationException exception) {
      return Optional.empty();
    }
  }
}
