package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import contrib.utils.components.item.ItemData;

import core.Entity;
import core.Game;
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
        new PositionComponent(droppedItem, new Point(0, 0));
        new DrawComponent(droppedItem, itemData.item().worldAnimation());
        new ItemComponent(droppedItem, itemData);
        new InteractionComponent(
                droppedItem,
                Constants.DEFAULT_ITEM_PICKUP_RADIUS,
                false,
                e -> {
                    Game.hero().ifPresent(hero -> itemData.triggerCollect(e, hero));
                });

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
