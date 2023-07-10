package contrib.components;

import contrib.utils.components.item.ItemData;

import core.Component;
import core.Entity;

/**
 * Marks an entity as an item in the game world.
 *
 * <p>In the dungeon, an item exists in two different states. The first state describes the item
 * when it is stored in an {@link InventoryComponent inventory}. An item within an inventory
 * consists only of the {@link ItemData} and is not an {@link Entity}. The second state is active
 * when an item is on the ground in the level. In this state, an item is an {@link Entity} with
 * various components. An entity with an {@link ItemComponent} is not an item in an inventory, but
 * an item that exists in the game world.
 *
 * <p>The {@link contrib.entities.WorldItemBuilder WorldItemBuilder} demonstrates how to create an
 * item entity. You can use the {@link contrib.entities.WorldItemBuilder#buildWorldItem(ItemData)}
 * method to create a new Item-Entity from an {@link ItemData}.
 *
 * <p>The {@link contrib.entities.WorldItemBuilder} will create an entity with a {@link
 * ItemComponent}, {@link core.components.PositionComponent}, {@link core.components.DrawComponent},
 * and {@link CollideComponent}. The {@link contrib.entities.WorldItemBuilder} will configure the
 * collide-callback for the Item-Entity so that if it collides with the player character, the stored
 * {@link ItemData} will be added to the {@link InventoryComponent} of the player, and the
 * item-entity will be removed from the game.
 *
 * <p>By default, an {@link ItemData} that is dropped from the inventory will be created as an
 * Item-Entity using the {@link contrib.entities.WorldItemBuilder}.
 *
 * <p>Note that this component does not implement this behavior. This component only marks an entity
 * as an Item and is used to store the associated {@link ItemData}.
 *
 * @see ItemData
 * @see contrib.entities.WorldItemBuilder
 */
public final class ItemComponent extends Component {
    private final ItemData itemData;

    /**
     * Creates a new {@link ItemComponent} and adds it to the associated entity.
     *
     * @param entity The associated entity.
     * @param itemData The data of the item to store in this component.
     */
    public ItemComponent(final Entity entity, ItemData itemData) {
        super(entity);
        this.itemData = itemData;
    }

    /**
     * Gets the {@link ItemData} of this component.
     *
     * @return The {@link ItemData} stored in this component.
     */
    public ItemData itemData() {
        return itemData;
    }
}
