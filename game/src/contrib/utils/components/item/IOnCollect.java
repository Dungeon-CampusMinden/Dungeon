package contrib.utils.components.item;

import core.Entity;

public interface IOnCollect {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
