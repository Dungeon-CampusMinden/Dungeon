package contrib.components;

import contrib.item.Item;
import core.Component;
import core.Entity;

/**
 * Marks an entity as an item in the game world.
 *
 * <p>In the dungeon, an item exists in two different states. The first state describes the item
 * when it is stored in an {@link InventoryComponent inventory}. An item within an inventory
 * consists only of the {@link Item} and is not an {@link Entity}. The second state is active when
 * an item is on the ground in the level. In this state, an item is an {@link Entity} with various
 * components. An entity with an {@link ItemComponent} is not an item in an inventory, but an item
 * that exists in the game world.
 *
 * <p>The {@link contrib.entities.WorldItemBuilder WorldItemBuilder} demonstrates how to create an
 * item entity. You can use the {@link contrib.entities.WorldItemBuilder#buildWorldItem(Item)}
 * method to create a new item-entity from an {@link Item}.
 *
 * <p>The {@link contrib.entities.WorldItemBuilder} will create an entity with a {@link
 * ItemComponent}, {@link core.components.PositionComponent}, {@link core.components.DrawComponent},
 * and {@link InteractionComponent}. The {@link contrib.entities.WorldItemBuilder} will configure
 * the interaction-callback for the item-entity so that if the player character interacts with it,
 * the stored {@link Item} will be added to the {@link InventoryComponent} of the player, and the
 * item-entity will be removed from the game.
 *
 * <p>By default, an {@link Item} that is dropped from the inventory will be (re-) created as an
 * item-entity using the {@link contrib.entities.WorldItemBuilder}.
 *
 * <p>Note that this component does not implement this behavior. This component only marks an entity
 * as an item and is used to store the associated {@link Item}.
 *
 * @see Item
 * @see contrib.entities.WorldItemBuilder
 */
public final class ItemComponent implements Component {
  private final Item itemData;

  /**
   * Creates a new {@link ItemComponent}.
   *
   * @param itemData The data of the item to store in this component.
   */
  public ItemComponent(final Item itemData) {
    this.itemData = itemData;
  }

  /**
   * Gets the {@link Item} of this component.
   *
   * @return The {@link Item} stored in this component.
   */
  public Item item() {
    return itemData;
  }
}
