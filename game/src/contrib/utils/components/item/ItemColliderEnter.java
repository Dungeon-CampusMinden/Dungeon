package contrib.utils.components.item;

import contrib.utils.components.collision.ICollide;
import core.Entity;
import core.level.Tile;

public class ItemColliderEnter implements ICollide {

    ItemData which;

    public ItemColliderEnter(ItemData which){
        this.which = which;
    }

    @Override
    public void onCollision(Entity a, Entity b, Tile.Direction from) {
        which.triggerCollect(a, b);
    }

    public ItemData getWhich() {
        return which;
    }
}
