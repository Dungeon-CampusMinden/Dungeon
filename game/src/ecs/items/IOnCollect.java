package ecs.items;

import java.io.Serializable;

import ecs.entities.Entity;

public interface IOnCollect extends Serializable {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
