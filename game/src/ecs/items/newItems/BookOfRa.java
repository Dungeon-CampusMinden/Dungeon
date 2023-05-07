package ecs.items.newItems;

import dslToGame.AnimationBuilder;
import ecs.entities.Entity;
import ecs.graphic.Animation;
import ecs.items.IOnCollect;
import ecs.items.ItemData;
import ecs.items.ItemType;
import ecs.items.WorldItemBuilder;
import starter.Game;

public class BookOfRa implements IOnCollect {
    private String world = "item/world/BookOfRa";
    private String inv = "item/world/BookOfRa";
    private final String name = "Book of Ra";
     private final String description = "Gives the owner on every new level a bonus of 1-5% bonus EP";
   ItemType passive = ItemType.Passive;
    Animation worldAnim = AnimationBuilder.buildAnimation(world);
    Animation bookAnim = AnimationBuilder.buildAnimation(inv);
    public BookOfRa(){
        Entity book = WorldItemBuilder.buildWorldItem(new ItemData(passive,worldAnim,bookAnim,name,description));
        onCollect(book, Game.getHero().get());
    }

    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {

    }
}
