package ecs.items;

import java.io.Serializable;

import ecs.entities.Entity;
import tools.Point;

public interface IOnDrop extends Serializable {
    void onDrop(Entity user, ItemData which, Point position);
}
