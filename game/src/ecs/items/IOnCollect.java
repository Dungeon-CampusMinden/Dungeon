package ecs.items;

import ecs.entities.Entity;

public interface IOnCollect {
    boolean onCollect(Entity WorldItemEntity, Entity whoCollides);
}
