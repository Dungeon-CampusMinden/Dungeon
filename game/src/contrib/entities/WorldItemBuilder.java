package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.ItemComponent;
import contrib.utils.components.item.ItemData;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;

import java.util.function.Consumer;

/** Class which creates all needed Components for a basic WorldItem */
public class WorldItemBuilder {

    /**
     * Creates an Entity which then can be added to the game
     *
     * @param itemData the Data which should be given to the world Item
     * @return the newly created Entity
     */
    public static Entity buildWorldItem(ItemData itemData, Point position) {
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, new Point(0, 0));
        new DrawComponent(droppedItem, itemData.worldTexture());
        new ItemComponent(droppedItem, itemData);
        CollideComponent component = new CollideComponent(droppedItem);
        component.collideEnter(
                (a, b, direction) -> {
                    itemData.triggerCollect(a, b);
                });
        return droppedItem;
    }
}
