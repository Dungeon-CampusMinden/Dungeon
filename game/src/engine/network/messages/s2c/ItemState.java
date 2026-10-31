package engine.network.messages.s2c;

import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable network representation of an item.
 *
 * @param itemType stable item registry ID
 * @param itemData serialized item-specific data
 */
public record ItemState(String itemType, Map<String, String> itemData) {

  /**
   * Creates an immutable item state.
   *
   * @param itemType stable item registry ID
   * @param itemData serialized item-specific data
   */
  public ItemState {
    if (itemType == null || itemType.isBlank()) {
      throw new IllegalArgumentException("itemType must not be null or blank.");
    }
    itemData = itemData == null ? Map.of() : Map.copyOf(itemData);
  }

  /**
   * Creates an item state from a domain item.
   *
   * @param item the domain item
   * @return immutable network state for the item
   */
  public static ItemState fromItem(Item item) {
    Objects.requireNonNull(item, "item");
    return new ItemState(ItemRegistry.idFor(item), item.itemData());
  }

  /**
   * Creates a fresh domain item from this network state.
   *
   * @return new domain item instance
   */
  public Item toItem() {
    return itemFromData()
        .orElseGet(
            () -> {
              Class<? extends Item> itemClass =
                  ItemRegistry.lookup(itemType)
                      .orElseThrow(
                          () -> new IllegalArgumentException("Unknown item type: " + itemType));
              return instantiate(itemClass);
            });
  }

  private Optional<Item> itemFromData() {
    if (itemData.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        ItemRegistry.create(itemType, itemData)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Item data provided but no factory registered for item type: "
                            + itemType)));
  }

  private static Item instantiate(Class<? extends Item> itemClass) {
    try {
      return itemClass.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException("Failed to instantiate item type: " + itemClass, e);
    }
  }
}
