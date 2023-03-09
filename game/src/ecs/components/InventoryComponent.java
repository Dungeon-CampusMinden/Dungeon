package ecs.components;

import ecs.entities.Entity;
import ecs.items.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import logging.CustomLogLevel;

public class InventoryComponent extends Component {

    private List<Item> inventory;
    private int maxSize;
    private final Logger inventoryLogger = Logger.getLogger(this.getClass().getName());

    /**
     * creates a new InventoryComponent
     *
     * @param entity the Entity where this Component should be added to
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
     * @param item the item which should be added
     * @return true if the item was added, otherwise false
     */
    public boolean addItem(Item item) {
        if (inventory.size() >= maxSize) return false;
        inventoryLogger.log(
                CustomLogLevel.DEBUG,
                "Item '"
                        + this.getClass().getSimpleName()
                        + "' was added to the inventory of entity '"
                        + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.add(item);
    }

    /**
     * removes the given Item from the inventory
     *
     * @param item the item which should be removed
     * @return true if the element was removed, otherwise false
     */
    public boolean removeItem(Item item) {
        inventoryLogger.log(
                CustomLogLevel.DEBUG,
                "Removing item '"
                        + this.getClass().getSimpleName()
                        + "' from inventory of entity '"
                        + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.remove(item);
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
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @return a copy of the inventory
     */
    public List<Item> getItems() {
        return new ArrayList<>(inventory);
    }
}
