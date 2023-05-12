package api.ecs.items;

import api.ecs.components.AnimationComponent;
import api.ecs.components.HitboxComponent;
import api.ecs.components.ItemComponent;
import api.ecs.components.PositionComponent;
import api.ecs.entities.Entity;
import api.tools.Point;

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
        new AnimationComponent(droppedItem, itemData.getWorldTexture());
        new ItemComponent(droppedItem, itemData);
        HitboxComponent component = new HitboxComponent(droppedItem);
        component.setiCollideEnter(
                (a, b, direction) -> {
                    itemData.triggerCollect(a, b);
                });
        return droppedItem;
    }
}
