package contrib.components;

import com.badlogic.gdx.utils.Null;
import contrib.item.Item;
import core.Component;
import core.utils.logging.CustomLogLevel;
import java.util.*;
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
   * <p>Items do get stacked, a stack will be split if needed.
   *
   * <p>Items are stored as a set, so an item instance cannot be stored twice in the same inventory
   * at the same time.
   *
   * @param item The item to be added.
   * @return True if the item was added, false if not.
   */
  public boolean add(final Item item) {
    if (addToStack(item) == 0) return true;

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

  private int addToStack(final Item item) {
    List<Item> sameClassItems = itemsOfSameClass(item);
    for (Item stack : sameClassItems) {
      if (item.stackSize() <= 0) {
        return 0;
      }
      int spaceLeft = stack.maxStackSize() - stack.stackSize();
      if (spaceLeft > 0) {
        int toTransfer = Math.min(spaceLeft, item.stackSize());
        stack.stackSize(stack.stackSize() + toTransfer);
        item.stackSize(item.stackSize() - toTransfer);
      }
    }

    return item.stackSize();
  }

  private List<Item> itemsOfSameClass(Item toGet) {
    List<Item> result = new ArrayList<>(this.inventory.length);
    for (Item invItem : inventory) {
      if (invItem != null && invItem.getClass().equals(toGet.getClass())) {
        result.add(invItem);
      }
    }
    return result;
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
   * Searches the Inventory for an item of the given class and returns it.
   *
   * @param klass The class of the item to search for.
   * @return an {@link Optional} containing the first found item of the given class, or {@link
   *     Optional#empty()} if none is found.
   */
  public Optional<Item> itemOfClass(final Class<? extends Item> klass) {
    return Arrays.stream(this.inventory).filter(klass::isInstance).findFirst();
  }

  /**
   * Gets the item with the smallest stack size out of all items of a given class.
   *
   * @param klass The class of the item to search for.
   * @return an {@link Optional} containing the item with the smallest stack size, or {@link
   *     Optional#empty()} if no such item is found.
   */
  public Optional<Item> smallestStackOfItemClass(final Class<? extends Item> klass) {
    return Arrays.stream(this.inventory)
        .filter(Objects::nonNull)
        .filter(klass::isInstance)
        .min(Comparator.comparingInt(Item::stackSize));
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
   * Returns the total number of items of the specified type in the inventory.
   *
   * <p>This method sums the {@link Item#stackSize()} of all items that are instances of the given
   * class.
   *
   * @param klass the class of items to count
   * @return the total number of items of the specified type
   */
  public int count(Class<? extends Item> klass) {
    return items(klass).stream().mapToInt(Item::stackSize).sum();
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

  /**
   * Removes one unit from the smallest stack of an item of the same class as the specified item in
   * the inventory.
   *
   * <p>If multiple stacks of items of the same class exist, the unit is removed from the stack with
   * the smaller size. If only one stack exists, the unit ist removed from that stack. If the stack
   * size reaches zero or below, the item is removed from the inventory entirely.
   *
   * @param item The reference item whose class is used to determine which item to remove one unit
   *     from.
   * @return true if one unit was successfully removed; false if no matching item was found in the
   *     inventory.
   */
  public boolean removeOne(Item item) {
    Item itemToRemoveOne =
        Arrays.stream(inventory)
                    .filter(Objects::nonNull)
                    .filter(it -> it.getClass().equals(item.getClass()))
                    .count()
                > 1
            ? smallestStackOfItemClass(item.getClass()).orElse(null)
            : item;

    if (itemToRemoveOne == null) {
      return false;
    }

    for (int i = 0; i < inventory.length; i++) {
      if (inventory[i] != null && inventory[i].equals(itemToRemoveOne)) {
        Item it = inventory[i];
        it.stackSize(it.stackSize() - 1);
        if (it.stackSize() <= 0) inventory[i] = null;
        return true;
      }
    }
    return false;
  }

  /**
   * Removes a specified number of items of a given type from the inventory.
   *
   * <p>If multiple stacks of the item exist, the method will remove items from each stack until the
   * requested amount has been removed. If a stack contains fewer items than needed, the entire
   * stack is removed and the method continues with the remaining amount. Partial stacks are reduced
   * accordingly.
   *
   * @param klass the class of the items to remove
   * @param amount the total number of items to remove
   */
  public void remove(Class<? extends Item> klass, int amount) {
    Set<Item> itemSet = this.items(klass);

    Iterator<Item> iterator = itemSet.iterator();
    while (iterator.hasNext() && amount > 0) {
      Item item = iterator.next();
      int stack = item.stackSize();

      if (stack <= amount) {
        amount -= stack;
        this.remove(item);
        iterator.remove(); // safe removal from Set
      } else {
        item.stackSize(stack - amount);
        amount = 0;
      }
    }
  }
}
