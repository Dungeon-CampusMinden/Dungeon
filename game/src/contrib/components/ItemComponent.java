package contrib.components;

import contrib.utils.components.item.ItemData;

import core.Component;
import core.Entity;
import core.utils.TriConsumer;

/**
 * Marks an entity as an item.
 *
 * <p>It contains the {@link #ItemData}, which contains all information about an item.
 *
 * <p>An entity with an {@link ItemComponent} is not an item in an inventory, but an item that
 * exists in the game world. Systems such as {@link contrib.systems.CollisionSystem CollisionSystem}
 * or {@link InteractionComponent} utilize the {@link ItemData} stored in this component to place
 * the item in the player's inventory upon collision or interaction. For this to work, the
 * associated entity needs not only the corresponding components, but also needs to implement the
 * callback functions (e.g., {@link CollideComponent#collideEnter(TriConsumer)}) of the respective
 * components with the logic for collecting the item.
 *
 * <p>Some default callback functions are already implemented in {@link ItemData} and can be used.
 *
 * <p>The {@link contrib.entities.WorldItemBuilder WorldItemBuilder} demonstrates how to create an
 * item entity.
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
