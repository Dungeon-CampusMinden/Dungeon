package ecs.items.newItems;

import dslToGame.AnimationBuilder;
import ecs.components.Component;
import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.graphic.Animation;
import ecs.items.*;
import starter.Game;
import tools.Point;

public class BookOfRa extends ItemData implements IOnCollect, IOnDrop {
    InventoryComponent inv;
    public BookOfRa(){
        super(
            ItemType.Passive,
            AnimationBuilder.buildAnimation("item/world/BookOfRa"),
            AnimationBuilder.buildAnimation("item/world/BookOfRa"),
            "Book of Ra",
            "Gives the owner a bonus EP of 1-5% every time he enters a new level"
        );

        Hero hero = null;
        if(Game.getHero().isPresent()){
            hero = (Hero) Game.getHero().get();
        }

        if(hero.getComponent(InventoryComponent.class).isPresent()){
            inv = (hero.getInv());
        }

        WorldItemBuilder.buildWorldItem(this);

        this.setOnCollect(this);
    }

    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        for(ItemData item: inv.getItems()){
            if(item instanceof  Bag bag){
                if(bag.addItem(this)){
                    Game.removeEntity(WorldItemEntity);
                    System.out.println(this.getItemName() + "has been added to the Book Bag.");
                }
            }
        }
    }


    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        //TODO
    }
}
