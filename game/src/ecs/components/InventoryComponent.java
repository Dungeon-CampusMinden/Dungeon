package ecs.components;

import ecs.components.ai.AITools;
import ecs.entities.Entity;
import ecs.items.ItemData;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ecs.items.ItemType;
import logging.CustomLogLevel;
import starter.Game;
import tools.Point;

/** Allows an Entity to carry Items */
public class InventoryComponent extends Component {
    private Entity entity;
    private List<ItemData> inventory;
    private int maxSize;
    private transient final Logger inventoryLogger = Logger.getLogger(this.getClass().getName());

    /**
     * creates a new InventoryComponent
     *
     * @param entity the Entity where this Component should be added to
     * @param maxSize the maximal size of the inventory
     */
    public InventoryComponent(Entity entity, int maxSize) {
        super(entity);
        this.entity = entity;
        if(maxSize > 9){
            maxSize = 9;
        }
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
        if (!itemData.getItemType().equals(ItemType.Bag)) {
            for(int index = 0; index < inventory.size(); index++){
                if(inventory.get(index).getItemType().equals(ItemType.Bag)){
                    if(inventory.get(index).getInventory().size() == 0){
                        addLog();
                        return inventory.get(index).getInventory().add(itemData);

                    }
                    else if (inventory.get(index).getInventory().size() < 3 && inventory.get(index).getInventory().get(0).getItemType().equals(itemData.getItemType())){
                        addLog();
                        return inventory.get(index).getInventory().add(itemData);
                    }
                }
            }
        }
        if(inventory.size() < maxSize){
            addLog();
            return inventory.add(itemData);
        }
        return false;
    }

    private void addLog(){
        inventoryLogger.log(
            CustomLogLevel.DEBUG,
            "Item '"
                + this.getClass().getSimpleName()
                + "' was added to the inventory of entity '"
                + entity.getClass().getSimpleName()
                + "'.");
    }

    public boolean useItem(int inventoryNumber){
        if(inventory.size() > inventoryNumber){
            ItemData itemData = inventory.get(inventoryNumber);
            itemData.triggerUse(entity);
            return true;
        }
        return false;
    }

    public void removeFirstItem(){
        if(inventory.size() > 0) {
            ItemData itemData = inventory.get(0);
            itemData.triggerDrop(entity, AITools.getRandomAccessibleTileCoordinateInRange(position(entity), 2f).toPoint());
        }
    }

    private Point position(Entity entity){
        Point entityPosition = ((PositionComponent) entity.getComponent(PositionComponent.class)
            .orElseThrow(
                () -> new MissingComponentException(
                    "PositionComponent")))
            .getPosition();
        return entityPosition;
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
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @return a copy of the inventory
     */
    public List<ItemData> getItems() {
        return new ArrayList<>(inventory);
    }
}
