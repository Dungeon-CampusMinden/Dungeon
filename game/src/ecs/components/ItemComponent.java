package ecs.components;

import ecs.entities.Entity;
import ecs.items.ItemData;

/** Marks an Entity as an Item. */
public class ItemComponent extends Component {
    private ItemData itemData;

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public ItemComponent(Entity entity) {
        super(entity);
    }

    /**
     * Creates a new ItemComponent and adds it to the associated entity
     *
     * @param entity associated entity
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
