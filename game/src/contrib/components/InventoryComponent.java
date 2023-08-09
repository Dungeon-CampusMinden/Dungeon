package contrib.components;

import contrib.utils.components.item.ItemData;

import core.Component;
import core.utils.logging.CustomLogLevel;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Allows the entity to collect items in an inventory.
 *
 * <p>The component stores a set of {@link ItemData} that the associated entity has collected.
 *
 * <p>Each inventory has a maximum number of item instances (items do not get stacked) that can be
 * carried.
 *
 * <p>Carried items can be retrieved using {@link #items() getItems}.
 *
 * <p>Items can be added via {@link #add(ItemData) addItem} and removed via {@link #remove(ItemData)
 * removeItem}.
 *
 * <p>The number of items in the inventory can be retrieved using {@link #count()}.
 */
public final class InventoryComponent implements Component {

    private final Set<ItemData> inventory;
    private final int maxSize;
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    /**
     * Create a new {@link InventoryComponent} with the given size.
     *
     * @param maxSize The number of items that can be stored in the inventory.
     */
    public InventoryComponent(int maxSize) {
        inventory = new HashSet<>(maxSize);
        this.maxSize = maxSize;
    }

    /**
     * Add the given item to the inventory.
     *
     * <p>Does not allow adding more items than the size of the inventory.
     *
     * <p>Items do not get stacked, so each instance will need space in the inventory.
     *
     * <p>Items are stored as a set, so an item instance cannot be stored twice in the same
     * inventory at the same time.
     *
     * @param itemData The item to be added.
     * @return True if the item was added, false if not.
     */
    public boolean add(final ItemData itemData) {
        if (inventory.size() >= maxSize) return false;
        LOGGER.log(
                CustomLogLevel.DEBUG,
                "Item '"
                        + this.getClass().getSimpleName()
                        + "' was added to the inventory of entity '"
                        // + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.add(itemData);
    }

    /**
     * Remove the given item from the inventory.
     *
     * @param itemData The item to be removed.
     * @return True if the item was removed, false otherwise.
     */
    public boolean remove(final ItemData itemData) {
        LOGGER.log(
                CustomLogLevel.DEBUG,
                "Removing item '" + this.getClass().getSimpleName() + "' from inventory.");
        return inventory.remove(itemData);
    }

    /**
     * Transfer the given item from this inventory to the given inventory.
     *
     * <p>If the given item is not present in this inventory or the other inventory is full, the
     * transfer will not be successful.
     *
     * <p>If the transfer was successful, the given item will be removed from this inventory.
     *
     * <p>Will not trigger {@link ItemData#onCollect()} or {@link ItemData#onDrop()}.
     *
     * <p>Cannot transfer the item to itself.
     *
     * @param itemData Item to transfer.
     * @param other {@link InventoryComponent} to transfer the item to.
     * @return true if the transfer was successful, false if not.
     */
    public boolean transfer(final ItemData itemData, final InventoryComponent other) {
        if (!other.equals(this) && inventory.contains(itemData) && other.add(itemData))
            return remove(itemData);
        return false;
    }

    /**
     * Get the number of items stored.
     *
     * @return The number of items that are stored in this component.
     */
    public int count() {
        return inventory.size();
    }

    /**
     * Get a Set of items stored in this component.
     *
     * @return A copy of the inventory.
     */
    public Set<ItemData> items() {
        return new HashSet<>(inventory);
    }
}
