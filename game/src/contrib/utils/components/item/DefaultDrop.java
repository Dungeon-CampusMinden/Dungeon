package contrib.utils.components.item;

import contrib.components.CollideComponent;
import contrib.utils.components.collision.ItemCollider;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.TriConsumer;

public class DefaultDrop implements TriConsumer<Entity, ItemData, Point> {
    @Override
    public void accept(Entity who, ItemData which, Point position) {
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, position);
        new DrawComponent(droppedItem, which.worldTexture());
        CollideComponent component = new CollideComponent(droppedItem);
        component.collideEnter(new ItemCollider(which));
    }
}
