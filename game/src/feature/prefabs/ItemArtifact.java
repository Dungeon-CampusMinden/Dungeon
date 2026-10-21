package feature.prefabs;

import feature.inventory.Item;
import feature.inventory.ItemRegistry;
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
   * @return the new item, or empty if the item class has no registered factory
   */
  public Optional<Item> create() {
    return ItemRegistry.create(itemId, itemData);
  }
}
