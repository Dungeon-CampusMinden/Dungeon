package ecs.entities;

import ecs.items.IOnCollect;
import ecs.items.IOnDrop;
import ecs.items.IOnUse;
import ecs.items.ItemData;
import tools.Point;

public abstract class Item extends Entity implements IOnCollect, IOnUse, IOnDrop {

    public Item() {
        super();
    }

    public abstract void setupAnimationComponent();

    public abstract void setupPositionComponent();

    public abstract void setupHitBoxComponent();

    public abstract void setupItemComponent();

    public abstract void onCollect(Entity WorldItemEntity, Entity whoCollides);

    public abstract void onUse(Entity e, ItemData item);

    public abstract void onDrop(Entity user, ItemData which, Point position);
}
