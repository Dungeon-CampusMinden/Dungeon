package contrib.utils.componentUtils.itemComponent;

import core.Entity;

public interface IOnCollect {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
