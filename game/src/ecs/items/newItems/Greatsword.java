package ecs.items.newItems;

import dslToGame.AnimationBuilder;
import ecs.components.HitboxComponent;
import ecs.entities.Entity;
import ecs.graphic.Animation;
import ecs.items.IOnCollect;
import ecs.items.ItemData;
import ecs.items.ItemType;
import ecs.items.WorldItemBuilder;

public class Greatsword implements IOnCollect {
    private String world = "item/world/Greatsword";
    private String greatsworld = "item/world/Greatsword";
    private final String name = "Greats Word";
    private final String description = "Increses the damage of the owner +20";
    ItemType passive = ItemType.Passive;
    Animation worldAnim = AnimationBuilder.buildAnimation(world);
    Animation greatsAnim = AnimationBuilder.buildAnimation(greatsworld);
    ItemData greats_word = new ItemData(passive,worldAnim,greatsAnim,name,description);

    public Greatsword(){
        new WorldItemBuilder();
        WorldItemBuilder.buildWorldItem(greats_word);
    }


    public void onCollect(Entity WorldItemEntity, Entity whoCollides){}

}
