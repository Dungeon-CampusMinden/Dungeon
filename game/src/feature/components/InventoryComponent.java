package feature.components;

import engine.Component;
import engine.Entity;
import engine.utils.Point;
import engine.utils.logging.DungeonLogger;
import feature.inventory.Item;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Allows the entity to collect items in an inventory.
 *
 * <p>The component stores the {@link Item}s the associated entity has collected in fixed slots.
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
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(InventoryComponent.class);

  private static final int DEFAULT_MAX_SIZE = 24;
  private Item[] inventory;
  // Callbacks, Consumer takes the added/removed Item as parameter (can be null if a null item was
  // added/removed)
  private Consumer<Item> onItemAdded = item -> {};
  private Consumer<Item> onItemRemoved = item -> {};
  // TODO: Only assignments read this; replace it with an inventory move event (#3299).
  private Move lastMove;

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
   * <p>The item occupies the next free slot. Does not allow adding more items than the size of the
   * inventory.
   *
   * @param item The item to be added.
   * @return True if the item was added, false if not.
   */
  public boolean add(final Item item) {
    int firstEmpty = this.findNextAvailableSlot();
    if (firstEmpty == -1) return false;
    return set(firstEmpty, item);
  }

  /**
   * Remove the given item instance from the inventory.
   *
   * @param item The item to be removed.
   * @return An {@link Optional} containing the removed item, or {@link Optional#empty()} if the
   *     item was not found.
   */
  public Optional<Item> remove(final Item item) {
    LOGGER.debug("Removing item '{}' from inventory.", this.getClass().getSimpleName());
    if (item == null) return Optional.empty();
    for (int i = 0; i < inventory.length; i++) {
      if (inventory[i] == item) {
        return remove(i);
      }
    }
    return Optional.empty();
  }

  /**
   * Remove item from specific index in inventory.
   *
   * @param index Index of item to remove.
   * @return An {@link Optional} containing the removed item, or {@link Optional#empty()} if no item
   *     was present at the given index.
   */
  public Optional<Item> remove(int index) {
    if (index >= this.inventory.length || index < 0) {
      LOGGER.warn("Tried to remove item at invalid inventory index: {}", index);
      return Optional.empty();
    }
    Item itemData = inventory[index];
    inventory[index] = null;
    this.onItemRemoved.accept(itemData);
    return Optional.ofNullable(itemData);
  }

  /**
   * Check if the inventory contains the given item.
   *
   * @param item Item to check for.
   * @return True if the inventory contains the item, false otherwise.
   */
  public boolean hasItem(final Item item) {
    return Arrays.stream(this.inventory).anyMatch(invItem -> invItem == item);
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
    LOGGER.debug(
        "Transferring item '{}' from inventory '{}' to inventory '{}'.",
        item.getClass().getSimpleName(),
        this.getClass().getSimpleName(),
        other.getClass().getSimpleName());
    if (!other.equals(this) && this.hasItem(item) && other.add(item))
      return this.remove(item).isPresent();
    return false;
  }

  /**
   * Transfer all items from this inventory to the given inventory.
   *
   * <p>Items that cannot be transferred (e.g., because the other inventory is full) will remain in
   * this inventory.
   *
   * @param other {@link InventoryComponent} to transfer the items to.
   * @return true if all items were successfully transferred, false if some items could not be
   *     transferred.
   */
  public boolean transferAll(final InventoryComponent other) {
    boolean allTransferred = true;
    for (Item item : this.items()) {
      if (item != null) {
        boolean transferred = this.transfer(item, other);
        if (!transferred) {
          allTransferred = false;
        }
      }
    }
    return allTransferred;
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
   * Returns the number of items of the specified type in the inventory.
   *
   * @param klass the class of items to count
   * @return the number of items that are instances of the given class
   */
  public int count(Class<? extends Item> klass) {
    return items(klass).size();
  }

  /**
   * Get the slots of this component.
   *
   * @return A copy of the inventory slots; empty slots are {@code null}.
   */
  public Item[] items() {
    return this.inventory.clone();
  }

  /**
   * Reconfigures this inventory for a newly selected character class.
   *
   * <p>Starting items belonging to the previous class are replaced by the new class's starting
   * items. Other items are retained, along with this component and its callbacks. If needed, the
   * inventory grows beyond the class capacity to avoid discarding existing items.
   *
   * @param maxSize the new inventory capacity
   * @param previousStartingItems starting items to remove from the previous class
   * @param newStartingItems starting items to add for the selected class
   */
  public void reconfigureForCharacterClass(
      int maxSize, List<Item> previousStartingItems, List<Item> newStartingItems) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Inventory size cannot be negative.");
    }

    List<Item> retainedItems =
        Arrays.stream(inventory)
            .filter(Objects::nonNull)
            .filter(item -> !containsSameInstance(previousStartingItems, item))
            .toList();
    inventory = new Item[Math.max(maxSize, newStartingItems.size() + retainedItems.size())];

    for (Item startingItem : newStartingItems) {
      add(startingItem);
    }

    for (Item item : retainedItems) {
      int slot = findNextAvailableSlot();
      if (slot == -1) {
        throw new IllegalStateException("Inventory capacity was insufficient to preserve items.");
      }
      inventory[slot] = item;
    }
  }

  private static boolean containsSameInstance(List<Item> items, Item candidate) {
    return items.stream().anyMatch(item -> item == candidate);
  }

  /**
   * Get the items stored in this component that are an instance of the given class.
   *
   * @param klass Only return items that are an instance of this class.
   * @return The matching items in slot order.
   */
  public List<Item> items(final Class<? extends Item> klass) {
    return Arrays.stream(this.inventory).filter(klass::isInstance).toList();
  }

  /**
   * Set the item at the given index.
   *
   * @param index Index of item to get.
   * @param item Item to set at index.
   * @return true if the item was set, false if the index is out of bounds.
   */
  public boolean set(int index, final Item item) {
    if (index >= this.inventory.length || index < 0) {
      LOGGER.warn("Tried to set item at invalid inventory index: {}", index);
      return false;
    }
    Item previous = this.inventory[index];
    if (previous == item) return true; // no change
    this.inventory[index] = item;
    if (previous != null) this.onItemRemoved.accept(previous);
    if (item != null) this.onItemAdded.accept(item);
    return true;
  }

  /**
   * Swap the items at the given indices without firing the added or removed callbacks, because
   * rearranging slots does not change what the inventory holds.
   *
   * @param first Index of the first slot.
   * @param second Index of the second slot.
   * @return true if the items were swapped, false if an index is out of bounds.
   */
  public boolean swap(int first, int second) {
    if (first < 0 || first >= inventory.length || second < 0 || second >= inventory.length) {
      LOGGER.warn("Tried to swap items at invalid inventory indices: {}, {}", first, second);
      return false;
    }
    Item item = inventory[first];
    inventory[first] = inventory[second];
    inventory[second] = item;
    return true;
  }

  /**
   * Get the item at the given index.
   *
   * @param index Index of item to get.
   * @return An {@link Optional} containing the item at the given index, or {@link Optional#empty()}
   *     if the index is out of bounds.
   */
  public Optional<Item> get(int index) {
    if (index >= this.inventory.length || index < 0) {
      LOGGER.warn("Tried to get item at invalid inventory index: {}", index);
      return Optional.empty();
    }
    return Optional.ofNullable(this.inventory[index]);
  }

  /**
   * Removes up to the given number of items of a given type from the inventory, in slot order.
   *
   * @param klass the class of the items to remove
   * @param amount the maximum number of items to remove
   */
  public void remove(Class<? extends Item> klass, int amount) {
    for (int i = 0; i < inventory.length && amount > 0; i++) {
      if (klass.isInstance(inventory[i])) {
        remove(i);
        amount--;
      }
    }
  }

  /**
   * Removes all items from the inventory.
   *
   * <p>This method clears the inventory by setting all item slots to null.
   */
  public void clear() {
    for (int i = 0; i < this.inventory.length; i++) {
      this.remove(i);
    }
  }

  /**
   * Finds the index of the next available (empty) slot in the inventory.
   *
   * @return The index of the next available slot, or -1 if the inventory is full.
   */
  public int findNextAvailableSlot() {
    for (int i = 0; i < this.inventory.length; i++) {
      if (this.inventory[i] == null) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Drops all items of the inventory at a given point.
   *
   * @param point position of the items on the ground.
   */
  public void dropAll(Point point) {
    for (Item item : items()) {
      if (item != null) {
        item.drop(point);
      }
    }
  }

  /**
   * Checks if the inventory is empty.
   *
   * @return true if the inventory contains no items, false otherwise.
   */
  public boolean isEmpty() {
    return count() == 0;
  }

  /**
   * Sets a callback function to be executed when an item is added to the inventory.
   *
   * <p>The consumer takes the added {@link Item} as its parameter (can be null if a null item was
   * added).
   *
   * @param onItemAdded A {@link Consumer} that defines the action to be performed when an item is
   *     added.
   */
  public void onItemAdded(Consumer<Item> onItemAdded) {
    this.onItemAdded = onItemAdded;
  }

  /**
   * Returns the callback function that is executed when an item is added to the inventory.
   *
   * <p>The consumer takes the added {@link Item} as its parameter (can be null if a null item was
   * added).
   *
   * @return A {@link Consumer} that defines the action performed when an item is added.
   */
  public Consumer<Item> onItemAdded() {
    return this.onItemAdded;
  }

  /**
   * Sets a callback function to be executed when an item is removed from the inventory.
   *
   * <p>The consumer takes the removed {@link Item} as its parameter (can be null if a null item was
   * removed).
   *
   * @param onItemRemoved A {@link Consumer} that defines the action to be performed when an item is
   *     removed.
   */
  public void onItemRemoved(Consumer<Item> onItemRemoved) {
    this.onItemRemoved = onItemRemoved;
  }

  /**
   * Returns the callback function that is executed when an item is removed from the inventory.
   *
   * <p>The consumer takes the removed {@link Item} as its parameter (can be null if a null item was
   * removed).
   *
   * @return A {@link Consumer} that defines the action performed when an item is removed.
   */
  public Consumer<Item> onItemRemoved() {
    return this.onItemRemoved;
  }

  /**
   * Returns the last item move into or out of this inventory by a player, e.g. through a chest's
   * inventory dialog.
   *
   * @return the last move, if any
   */
  public Optional<Move> lastMove() {
    return Optional.ofNullable(lastMove);
  }

  /**
   * Records that a player just moved an item into or out of this inventory.
   *
   * @param player the moving player
   */
  public void recordMove(final Entity player) {
    this.lastMove = new Move(Objects.requireNonNull(player, "player"), System.nanoTime());
  }

  /**
   * One item move by a player.
   *
   * @param player the moving player
   * @param nanoTime {@link System#nanoTime()} of the move, to order moves across inventories
   */
  public record Move(Entity player, long nanoTime) {}
}
