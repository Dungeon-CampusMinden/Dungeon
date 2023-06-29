package contrib.utils.components.item;

import contrib.components.CollideComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;

public class DefaultDrop implements IOnDrop{
    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, position);
        new DrawComponent(droppedItem, which.getWorldTexture());
        CollideComponent component = new CollideComponent(droppedItem);
        //component.setiCollideEnter((a, b, direction) -> which.triggerCollect(a, b));
        component.setiCollideEnter(new ItemColliderEnter(which));
    }
}
