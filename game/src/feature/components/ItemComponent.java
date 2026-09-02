package feature.components;

import engine.Component;
import engine.Entity;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;

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
 * <p>The {@link feature.entities.WorldItemBuilder WorldItemBuilder} demonstrates how to create an
 * item entity. You can use the {@link feature.entities.WorldItemBuilder#buildWorldItem(Item)}
 * method to create a new item-entity from an {@link Item}.
 *
 * <p>The {@link feature.entities.WorldItemBuilder} will create an entity with a {@link
 * ItemComponent}, {@link engine.components.PositionComponent}, {@link
 * engine.components.DrawComponent}, and {@link InteractionComponent}. The {@link
 * feature.entities.WorldItemBuilder} will configure the interaction-callback for the item-entity so
 * that if the player character interacts with it, the stored {@link Item} will be added to the
 * {@link InventoryComponent} of the player, and the item-entity will be removed from the game.
 *
 * <p>By default, an {@link Item} that is dropped from the inventory will be (re-) created as an
 * item-entity using the {@link feature.entities.WorldItemBuilder}.
 *
 * <p>Note that this component does not implement this behavior. This component only marks an entity
 * as an item and is used to store the associated {@link Item}.
 *
 * @see Item
 * @see feature.entities.WorldItemBuilder
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
