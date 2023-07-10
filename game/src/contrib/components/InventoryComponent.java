package contrib.components;

import contrib.utils.components.item.ItemData;

import core.Component;
import core.Entity;
import core.utils.logging.CustomLogLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Allows the entity to collect items in an inventory.
 *
 * <p>The component stores a collection of {@link ItemData} that are currently stored in the
 * associated entity.
 *
 * <p>Each inventory has a maximum number of items that can be carried.
 *
 * <p>Carried items can be retrieved using {@link #items() getItems}.
 *
 * <p>Items can be added via {@link #add(ItemData) addItem} and removed via {@link #remove(ItemData)
 * removeItem}.
 *
 * <p>The number of items in the inventory can be retrieved using {@link #count() filledSlots}, and
 * the number of items that can still be added can be retrieved using {@link #freeSpace()
 * emptySlots}. The maximum inventory size can also be retrieved using {@link #maxSize()
 * getMaxSize}.
 */
public final class InventoryComponent extends Component {

    private final List<ItemData> inventory;
    private final int maxSize;
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    /**
     * Create a new {@link InventoryComponent} with the given size and add it to the associated
     * entity.
     *
     * @param entity The associated entity.
     * @param maxSize The number of items that can be stored in the inventory.
     */
    public InventoryComponent(final Entity entity, int maxSize) {
        super(entity);
        inventory = new ArrayList<>(maxSize);
        this.maxSize = maxSize;
    }

    /**
     * Add the given item to the inventory.
     *
     * <p>Does not allow adding more items than the size of the inventory.
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
                        + entity.getClass().getSimpleName()
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
                "Removing item '"
                        + this.getClass().getSimpleName()
                        + "' from inventory of entity '"
                        + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.remove(itemData);
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
     * Get the available number of items that can still be stored.
     *
     * @return The size of the available item space.
     */
    public int freeSpace() {
        return maxSize - inventory.size();
    }

    /**
     * Get the maximum number of items that can be stored in this component.
     *
     * @return The size of the inventory.
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Get a list of items stored in this component.
     *
     * @return A copy of the inventory.
     */
    public List<ItemData> items() {
        return new ArrayList<>(inventory);
    }
}
