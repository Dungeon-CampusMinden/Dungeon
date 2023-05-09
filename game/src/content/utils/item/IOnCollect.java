package content.utils.item;

import api.Entity;

public interface IOnCollect {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
