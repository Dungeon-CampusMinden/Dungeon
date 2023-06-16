package contrib.components;

import contrib.utils.components.item.ItemData;

import core.Component;
import core.Entity;
import core.utils.logging.CustomLogLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This Component marks an {@link Entity} as having an inventory
 *
 * <p>It keeps track of the currently carried items in the list {@link #inventory} and the size of
 * the inventory in {@link #maxSize}. Carried items can be retrieved by using {@link #items()
 * getItems}. List elements can be added via {@link #addItem(ItemData) addItem} and removed via
 * {@link #removeItem(ItemData) removeItem}.
 *
 * <p>The number of filled slots can be retrieved via {@link #filledSlots() filledSlots} and the
 * number of empty slots via {@link #emptySlots() emptySlots}. The maximum inventory size can also
 * be retrieved via {@link #maxSize() getMaxSize}.
 */
public class InventoryComponent extends Component {

    private List<ItemData> inventory;
    private int maxSize;
    private final Logger inventoryLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates a new InventoryComponent
     *
     * <p>Create a new InventoryComponent by explicitly setting the maximum inventory size.
     *
     * @param entity the Entity which will have the inventory added
     * @param maxSize the maximal size of the inventory
     */
    public InventoryComponent(Entity entity, int maxSize) {
        super(entity);
        inventory = new ArrayList<>(maxSize);
        this.maxSize = maxSize;
    }

    /**
     * Adding an Element to the Inventory does not allow adding more items than the size of the
     * Inventory.
     *
     * @param itemData the item which should be added
     * @return true if the item was added, otherwise false
     */
    public boolean addItem(ItemData itemData) {
        if (inventory.size() >= maxSize) return false;
        inventoryLogger.log(
                CustomLogLevel.DEBUG,
                "Item '"
                        + this.getClass().getSimpleName()
                        + "' was added to the inventory of entity '"
                        + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.add(itemData);
    }

    /**
     * removes the given Item from the inventory
     *
     * @param itemData the item which should be removed
     * @return true if the element was removed, otherwise false
     */
    public boolean removeItem(ItemData itemData) {
        inventoryLogger.log(
                CustomLogLevel.DEBUG,
                "Removing item '"
                        + this.getClass().getSimpleName()
                        + "' from inventory of entity '"
                        + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.remove(itemData);
    }

    /**
     * @return the number of slots already filled with items
     */
    public int filledSlots() {
        return inventory.size();
    }

    /**
     * @return the number of slots still empty
     */
    public int emptySlots() {
        return maxSize - inventory.size();
    }

    /**
     * @return the size of the inventory
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * @return a copy of the inventory
     */
    public List<ItemData> items() {
        return new ArrayList<>(inventory);
    }
}
