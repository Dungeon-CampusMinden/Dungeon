package ecs.items.newItems;

import dslToGame.AnimationBuilder;
import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import ecs.graphic.Animation;
import ecs.items.IOnCollect;
import ecs.items.ItemData;
import ecs.items.ItemType;
import ecs.items.WorldItemBuilder;
import starter.Game;

public class Bag extends ItemData implements IOnCollect {

    private String world = "item/world/Bag";
    private String greatsworld = "item/world/Bag";
    private final String name = "Bag";
    private final String description = "A Bag which is capable of carry 4 item of the same type";
    ItemType active = ItemType.Active;
    Animation worldAnim = AnimationBuilder.buildAnimation(world);
    Animation greatsAnim = AnimationBuilder.buildAnimation(greatsworld);

    public Bag(){
        WorldItemBuilder.buildWorldItem(new ItemData(active,worldAnim,greatsAnim,name,description));
    }

    public void onCollect(Entity WorldItemEntity, Entity whoCollides){

    };
}
