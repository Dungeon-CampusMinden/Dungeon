package ecs.items;

import java.io.Serializable;

import ecs.entities.Entity;
/*
 * This interface is used to implement the strategy pattern for items.
 * The idea is that when an item is collected, the item will call the
 * onCollect method of the item. This method will then be implemented
 * by the item to do whatever it needs to do when it is collected.
 * This allows us to have different items that do different things
 * when they are collected.
 */
public interface IOnCollect extends Serializable {
    void onCollect(Entity WorldItemEntity, Entity whoCollides);
}
