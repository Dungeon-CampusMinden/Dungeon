package api.utils.component_utils.itemComponent;

import api.Entity;

public interface IOnCollect {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
