package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import contrib.utils.components.item.ItemData;

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
     * @param itemData the Data which should be given to the world Item
     * @return the newly created Entity
     */
    public static Entity buildWorldItem(ItemData itemData) {
        Entity droppedItem = new Entity();
        droppedItem.addComponent(new PositionComponent(new Point(0, 0)));
        droppedItem.addComponent(new DrawComponent(itemData.item().worldAnimation()));
        droppedItem.addComponent(new ItemComponent(itemData));
        droppedItem.addComponent(
                new InteractionComponent(
                        Constants.DEFAULT_ITEM_PICKUP_RADIUS, true, itemData.onCollect()));

        return droppedItem;
    }

    /**
     * Creates an Entity which then can be added to the game
     *
     * @param itemData the Data which should be given to the world Item
     * @param position the position where the item should be placed
     * @return the newly created Entity
     */
    public static Entity buildWorldItem(ItemData itemData, Point position) {
        Entity droppedItem = buildWorldItem(itemData);
        droppedItem.fetch(PositionComponent.class).ifPresent(pc -> pc.position(position));
        return droppedItem;
    }
}
