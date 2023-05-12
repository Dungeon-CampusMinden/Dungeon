package ecs.items.newItems;

import dslToGame.AnimationBuilder;
import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.items.*;
import java.util.ArrayList;
import java.util.List;
import starter.Game;

/**
 * A bag is an item that kinda acts like an InventoryComponent, but it only can carry items of the
 * Instance BookOfRa.
 */
public class Bag extends ItemData implements IOnCollect {

    private List<ItemData> inventory;

    private final int maxSize = 4;

    public Bag(ItemType itemType) {
        super(
                ItemType.Bag,
                AnimationBuilder.buildAnimation("item/world/Bag"),
                AnimationBuilder.buildAnimation("item/world/Bag"),
                "Bag",
                "A bag which is capable of carrying 4 items of the same type.");

        WorldItemBuilder.buildWorldItem(this);
        inventory = new ArrayList<>(4);
    }

    /**
     * Adds item to hero inventory and delete object from world.
     *
     * @param WorldItemEntity
     * @param whoCollides
     */
    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        if (whoCollides instanceof Hero hero) {
            Game.removeEntity(WorldItemEntity);
            InventoryComponent inv = hero.getInv();
            inv.addItem(this);
        }
    }

    /**
     * Adds item to bag
     *
     * @param itemData - Item to add
     */
    public boolean addItem(ItemData itemData) {
        if (inventory.size() >= maxSize) return false;

        return inventory.add(itemData);
    }

    /**
     * Removes item from bag
     *
     * @param itemData - Item to remove
     */
    public boolean removeItem(ItemData itemData) {

        return inventory.remove(itemData);
    }

    /** Return the current amount of items in bag */
    public int filledSlots() {
        return inventory.size();
    }

    /** Returns current amount of available slots in bag */
    public int emptySlots() {
        return maxSize - inventory.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    /** Returns list of items in bag */
    public List<ItemData> getItems() {
        return new ArrayList<>(inventory);
    }
}
