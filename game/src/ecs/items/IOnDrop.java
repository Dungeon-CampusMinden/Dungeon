package ecs.items;

import java.io.Serializable;

import ecs.entities.Entity;
import tools.Point;
/*
 * IOnDrop is an interface that is used to define the behavior of an item when it is dropped.
 * It is used in the Item class.
 */
public interface IOnDrop extends Serializable {
    void onDrop(Entity user, ItemData which, Point position);
}
