package ecs.items;

import ecs.entities.Entity;
import tools.Point;

public interface IOnDrop {
    void onDrop(Entity user, ItemData which, Point position);
}
