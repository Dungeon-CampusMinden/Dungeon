package contrib.components;

import contrib.utils.components.item.ItemData;

import core.Component;
import core.Entity;

/**
 * A class that marks an entity as an Item and controls its {@link ItemData}
 *
 * <p> It contains the {@link #itemData}, which contains all info about the Item.
 */
public class ItemComponent extends Component {
    private ItemData itemData;

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity entity that will be marked as an Item
     */
    public ItemComponent(Entity entity) {
        super(entity);
    }

    /**
     * Creates a new ItemComponent and adds it to the associated entity
     *
     * @param entity entity that will be marked as an Item
     * @param itemData data of the item for the component
     */
    public ItemComponent(Entity entity, ItemData itemData) {
        super(entity);
        this.itemData = itemData;
    }

    /**
     * @return the ItemData
     */
    public ItemData getItemData() {
        return itemData;
    }

    /**
     * @param itemData data of the item for the component
     */
    public void setItemData(ItemData itemData) {
        this.itemData = itemData;
    }
}
