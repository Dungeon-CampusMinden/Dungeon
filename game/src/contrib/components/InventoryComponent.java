package contrib.components;

import com.badlogic.gdx.utils.Null;

import contrib.utils.components.item.ItemData;

import core.Component;
import core.utils.logging.CustomLogLevel;

import java.util.Arrays;
import java.util.Objects;
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

    private final ItemData[] inventory;
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    /**
     * Create a new {@link InventoryComponent} with the given size.
     *
     * @param maxSize The number of items that can be stored in the inventory.
     */
    public InventoryComponent(int maxSize) {
        inventory = new ItemData[maxSize];
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
        inventory[firstEmpty] = itemData;
        return true;
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
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].equals(itemData)) {
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
    public ItemData remove(int index) {
        ItemData itemData = inventory[index];
        inventory[index] = null;
        return itemData;
    }

    /**
     * Check if the inventory contains the given item.
     *
     * @param itemData Item to check for.
     * @return True if the inventory contains the item, false otherwise.
     */
    public boolean hasItem(ItemData itemData) {
        return Arrays.stream(this.inventory)
                .anyMatch(item -> item != null && item.equals(itemData));
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
        if (!other.equals(this) && this.hasItem(itemData) && other.add(itemData))
            return this.remove(itemData);
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
    public ItemData[] items() {
        return this.inventory.clone();
    }

    /**
     * Set the item at the given index.
     *
     * @param index Index of item to get.
     * @param itemData Item to set at index.
     */
    public void set(int index, @Null ItemData itemData) {
        if (index >= this.inventory.length || index < 0) return;
        this.inventory[index % this.inventory.length] = itemData;
    }

    /**
     * Get the item at the given index.
     *
     * @param index Index of item to get.
     * @return Item at index. May be null.
     */
    @Null
    public ItemData get(int index) {
        if (index >= this.inventory.length || index < 0) return null;
        return this.inventory[index];
    }
}
