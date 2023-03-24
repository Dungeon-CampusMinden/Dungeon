package ecs.items;

import ecs.entities.Entity;

/** Interface for ItemUsable. Implements the callback for when the item is used. */
public interface IOnUse {

    /**
     * Called when the item is used.
     *
     * @param e The entity that used the item.
     * @param item The item that was used.
     */
    void onUse(Entity e, ItemData item);
}
