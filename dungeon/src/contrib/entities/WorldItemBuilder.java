package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import contrib.item.Item;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;

/** Class which creates all needed Components for a basic WorldItem. */
public final class WorldItemBuilder {
  private static final float DEFAULT_ITEM_PICKUP_RADIUS = 2.0f;

  /**
   * Creates an Entity which then can be added to the game.
   *
   * @param item the Item that is stored in the entity
   * @return the newly created Entity
   */
  public static Entity buildWorldItem(final Item item) {
    Entity droppedItem = new Entity();
    droppedItem.add(new PositionComponent(PositionComponent.ILLEGAL_POSITION));
    droppedItem.add(new DrawComponent(item.worldAnimation()));
    droppedItem.add(new ItemComponent(item));

    droppedItem.add(new InteractionComponent(DEFAULT_ITEM_PICKUP_RADIUS, true, item::collect));

    return droppedItem;
  }

  /**
   * Creates an Entity which then can be added to the game.
   *
   * @param item the Data which should be given to the world Item
   * @param position the position where the item should be placed
   * @return the newly created Entity
   */
  public static Entity buildWorldItem(final Item item, final Point position) {
    Entity droppedItem = buildWorldItem(item);
    droppedItem.fetch(PositionComponent.class).ifPresent(pc -> pc.position(position));
    return droppedItem;
  }
}
