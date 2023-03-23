package ecs.items;

import ecs.entities.Entity;
import tools.Point;

public interface IOnDrop {
    boolean onDrop(Entity user, ItemData which, Point position);
}
