package contrib.item;

import java.io.Serial;
import java.io.Serializable;

/**
 * Compact representation of an {@link Item} for network transmission.
 *
 * <p>Instead of serializing the full Item object (which includes Animation objects with texture
 * paths, configs, etc.), this snapshot contains only the essential network data: the item's class
 * name and stack size.
 *
 * <p>The client reconstructs the full Item from the registered item class using {@link
 * Item#getItem(String)} and the stack size.
 *
 * @param itemClass The simple class name of the item (e.g., "ItemWoodenBow")
 * @param stackSize The number of items in this stack (max 64, using byte for compact serialization)
 */
public record ItemSnapshot(String itemClass, byte stackSize) implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Creates an ItemSnapshot from an Item.
   *
   * @param item the item to snapshot, or null
   * @return an ItemSnapshot, or null if item is null
   */
  public static ItemSnapshot from(Item item) {
    if (item == null) return null;
    return new ItemSnapshot(item.getClass().getSimpleName(), item.stackSize());
  }

  /**
   * Reconstructs an Item from this snapshot.
   *
   * @return a new Item instance, or null if the item class is not registered
   */
  public Item toItem() {
    if (itemClass == null) return null;
    Class<? extends Item> itemClass = Item.getItem(this.itemClass);
    if (itemClass == null) {
      return null; // Item class not found
    }
    try {
      Item item = itemClass.getDeclaredConstructor().newInstance();
      item.stackSize(stackSize);
      return item;
    } catch (Exception e) {
      return null; // Failed to instantiate
    }
  }
}
