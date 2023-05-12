package api.utils.componentUtils.itemComponent;

import api.Entity;

public interface IOnCollect {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
