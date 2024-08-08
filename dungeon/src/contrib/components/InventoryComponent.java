package contrib.components;

import com.badlogic.gdx.utils.Null;
import contrib.item.Item;
import core.Component;
import core.utils.logging.CustomLogLevel;
import dsl.annotation.DSLType;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Allows the entity to collect items in an inventory.
 *
 * <p>The component stores a set of {@link Item} that the associated entity has collected.
 *
 * <p>Each inventory has a maximum number of item instances (items do not get stacked) that can be
 * carried.
 *
 * <p>Carried items can be retrieved using {@link #items() getItems}.
 *
 * <p>Items can be added via {@link #add(Item) addItem} and removed via {@link #remove(Item)
 * removeItem}.
 *
 * <p>The number of items in the inventory can be retrieved using {@link #count()}.
 */
@DSLType
public final class InventoryComponent implements Component {

  private static final int DEFAULT_MAX_SIZE = 24;
  private final Item[] inventory;
  private final Logger LOGGER = Logger.getLogger(InventoryComponent.class.getSimpleName());

  /**
   * The default {@link InventoryComponent} constructor. Creates an empty inventory with {@link
   * #DEFAULT_MAX_SIZE} slots.
   */
  public InventoryComponent() {
    inventory = new Item[DEFAULT_MAX_SIZE];
  }

  /**
   * Create a new {@link InventoryComponent} with the given size.
   *
   * @param maxSize The number of items that can be stored in the inventory.
   */
  public InventoryComponent(int maxSize) {
    inventory = new Item[maxSize];
  }

  /**
   * Add the given item to the inventory.
   *
   * <p>Does not allow adding more items than the size of the inventory.
   *
   * <p>Items do not get stacked, so each instance will need space in the inventory.
   *
   * <p>Items are stored as a set, so an item instance cannot be stored twice in the same inventory
   * at the same time.
   *
   * @param item The item to be added.
   * @return True if the item was added, false if not.
   */
  public boolean add(final Item item) {
    int firstEmpty = -1;
    for (int i = 0; i < this.inventory.length; i++) {
      if (this.inventory[i] == null) {
        firstEmpty = i;
        break;
      }
    }
    if (firstEmpty == -1) return false;
    LOGGER.log(
        CustomLogLevel.DEBUG,
        "Item '"
            + this.getClass().getSimpleName()
            + "' was added to the inventory of entity '"
            + "'.");
    inventory[firstEmpty] = item;
    return true;
  }

  /**
   * Remove the given item from the inventory.
   *
   * @param item The item to be removed.
   * @return True if the item was removed, false otherwise.
   */
  public boolean remove(final Item item) {
    LOGGER.log(
        CustomLogLevel.DEBUG,
        "Removing item '" + this.getClass().getSimpleName() + "' from inventory.");
    for (int i = 0; i < inventory.length; i++) {
      if (inventory[i] != null && inventory[i].equals(item)) {
        inventory[i] = null;
        return true;
      }
    }
    return false;
  }

  /**
   * Remove item from specific index in inventory.
   *
   * @param index Index of item to remove.
   * @return Item removed. May be null.
   */
  @Null
  public Item remove(int index) {
    Item itemData = inventory[index];
    inventory[index] = null;
    return itemData;
  }

  /**
   * Check if the inventory contains the given item.
   *
   * @param item Item to check for.
   * @return True if the inventory contains the item, false otherwise.
   */
  public boolean hasItem(final Item item) {
    return Arrays.stream(this.inventory)
        .anyMatch(invItem -> invItem != null && invItem.equals(item));
  }

  /**
   * Checks if the inventory contains an item of the specified class.
   *
   * <p>This method uses Java Streams to iterate over the inventory array and checks if any item is
   * an instance of the specified class.
   *
   * @param klass The class of the item to check for in the inventory.
   * @return True if the inventory contains an item of the specified class, false otherwise.
   */
  public boolean hasItem(final Class<? extends Item> klass) {
    return Arrays.stream(this.inventory).anyMatch(klass::isInstance);
  }

  /**
   * Transfer the given item from this inventory to the given inventory.
   *
   * <p>If the given item is not present in this inventory or the other inventory is full, the
   * transfer will not be successful.
   *
   * <p>If the transfer was successful, the given item will be removed from this inventory.
   *
   * <p>Cannot transfer the item to itself.
   *
   * @param item Item to transfer.
   * @param other {@link InventoryComponent} to transfer the item to.
   * @return true if the transfer was successful, false if not.
   */
  public boolean transfer(final Item item, final InventoryComponent other) {
    if (!other.equals(this) && this.hasItem(item) && other.add(item)) return this.remove(item);
    return false;
  }

  /**
   * Get the number of items stored.
   *
   * @return The number of items that are stored in this component.
   */
  public int count() {
    return (int) Arrays.stream(this.inventory).filter(Objects::nonNull).count();
  }

  /**
   * Get a Set of items stored in this component.
   *
   * @return A copy of the inventory.
   */
  public Item[] items() {
    return this.inventory.clone();
  }

  /**
   * Get a Set of items stored in this component that are an instance of the given class.
   *
   * @param klass Only return items that are an instance of this class.
   * @return A Set of items that are in this Inventory and are an instance of the given class.
   */
  public Set<Item> items(final Class<? extends Item> klass) {
    return Arrays.stream(this.inventory.clone())
        .filter(klass::isInstance)
        .collect(Collectors.toSet());
  }

  /**
   * Set the item at the given index.
   *
   * @param index Index of item to get.
   * @param item Item to set at index.
   */
  public void set(int index, final Item item) {
    if (index >= this.inventory.length || index < 0) return;
    this.inventory[index % this.inventory.length] = item;
  }

  /**
   * Get the item at the given index.
   *
   * @param index Index of item to get.
   * @return Item at index. May be null.
   */
  @Null
  public Item get(int index) {
    if (index >= this.inventory.length || index < 0) return null;
    return this.inventory[index];
  }
}
