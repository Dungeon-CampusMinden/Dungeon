package content.entity;

import api.Entity;
import api.components.CollideComponent;
import api.components.DrawComponent;
import api.components.ItemComponent;
import api.components.PositionComponent;
import api.utils.Point;
import api.utils.component_utils.itemComponent.ItemData;

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
        new DrawComponent(droppedItem, itemData.getWorldTexture());
        new ItemComponent(droppedItem, itemData);
        CollideComponent component = new CollideComponent(droppedItem);
        component.setiCollideEnter(
                (a, b, direction) -> {
                    itemData.triggerCollect(a, b);
                });
        return droppedItem;
    }
}
