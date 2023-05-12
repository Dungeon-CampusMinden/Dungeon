package api.ecs.items;

import api.ecs.entities.Entity;

public interface IOnCollect {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
