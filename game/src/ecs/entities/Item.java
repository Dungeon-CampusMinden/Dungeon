package ecs.entities;

import ecs.items.IOnCollect;
import ecs.items.IOnDrop;
import ecs.items.IOnUse;
import ecs.items.ItemData;
import tools.Point;
/**
 * The Item is a entity in the ECS. This class helps to setup items with all its
 * components and attributes .
 * It is a abstract class, so it can be extended by other items.
 * It has the onUse, onDrop and onCollect methods, which are called when the item is used, dropped or collected.
 */
public abstract class Item extends Entity implements IOnCollect, IOnUse, IOnDrop {

    public Item() {
        super();
    }

    protected abstract void setupAnimationComponent();

    protected abstract void setupPositionComponent();

    protected abstract void setupHitBoxComponent();

    protected abstract void setupItemComponent();

    public abstract void onCollect(Entity WorldItemEntity, Entity whoCollides);

    public abstract void onUse(Entity e, ItemData item);

    public abstract void onDrop(Entity user, ItemData which, Point position);
}
