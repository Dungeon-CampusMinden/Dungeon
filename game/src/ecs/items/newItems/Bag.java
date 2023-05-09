package ecs.items.newItems;

import dslToGame.AnimationBuilder;
import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.graphic.Animation;
import ecs.items.*;
import logging.CustomLogLevel;
import starter.Game;
import tools.Point;

import java.util.ArrayList;
import java.util.List;

public class Bag extends ItemData implements IOnCollect, IOnDrop {


    private List<ItemData> inventory;

    private int maxSize;

    public Bag(ItemType itemType){
        super(ItemType.Bag,
            AnimationBuilder.buildAnimation("item/world/Bag"),
            AnimationBuilder.buildAnimation("item/world/Bag"),
            "Bag",
            "A bag which is capable of carrying 4 items of the same type.");

        WorldItemBuilder.buildWorldItem(this);

    }

    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides){
        if (whoCollides instanceof Hero hero){
            Game.removeEntity(WorldItemEntity);
            InventoryComponent inv = hero.getInv();
            inv.addItem(this);
        }
    }

    @Override
    public void onDrop(Entity user, ItemData which, Point position) {

    }

    public boolean addItem(ItemData itemData) {
        if (inventory.size() >= maxSize) return false;


        return inventory.add(itemData);
    }

    public boolean removeItem(ItemData itemData) {

        return inventory.remove(itemData);
    }

    public int filledSlots() {
        return inventory.size();
    }

    public int emptySlots() {
        return maxSize - inventory.size();
    }

    public int getMaxSize() {
        return maxSize;
    }
    public List<ItemData> getItems() {
        return new ArrayList<>(inventory);
    }


}
