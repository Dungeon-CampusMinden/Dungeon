package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import contrib.item.IItemCollectable;
import contrib.item.Item;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Constants;
import core.utils.Point;

/** Class which creates all needed Components for a basic WorldItem */
public class WorldItemBuilder {

    /**
     * Creates an Entity which then can be added to the game
     *
     * @param item the Item that is stored in the entity
     * @return the newly created Entity
     */
    public static Entity buildWorldItem(Item item) {
        Entity droppedItem = new Entity();
        droppedItem.addComponent(new PositionComponent(new Point(0, 0)));
        droppedItem.addComponent(new DrawComponent(item.worldAnimation()));
        droppedItem.addComponent(new ItemComponent(item));

        if (item instanceof IItemCollectable itemCollectable) {
            droppedItem.addComponent(
                    new InteractionComponent(
                            Constants.DEFAULT_ITEM_PICKUP_RADIUS, false, itemCollectable::collect));
        }

        return droppedItem;
    }

    /**
     * Creates an Entity which then can be added to the game
     *
     * @param item the Data which should be given to the world Item
     * @param position the position where the item should be placed
     * @return the newly created Entity
     */
    public static Entity buildWorldItem(Item item, Point position) {
        Entity droppedItem = buildWorldItem(item);
        droppedItem.fetch(PositionComponent.class).ifPresent(pc -> pc.position(position));
        return droppedItem;
    }
}
