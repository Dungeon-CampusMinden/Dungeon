package contrib.utils.components.collision;

import contrib.utils.components.item.ItemData;

import core.Entity;
import core.level.Tile;
import core.utils.TriConsumer;

public class ItemCollider implements TriConsumer<Entity, Entity, Tile.Direction> {

    private ItemData which;

    public ItemCollider(ItemData which) {
        this.which = which;
    }

    @Override
    public void accept(Entity a, Entity b, Tile.Direction direction) {
        which.triggerCollect(a, b);
    }

    public ItemData getWhich() {
        return which;
    }
}
