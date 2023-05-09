package ecs.items.newItems;

import dslToGame.AnimationBuilder;

import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.graphic.Animation;
import ecs.items.*;
import starter.Game;
import tools.Point;

public class Greatsword extends ItemData implements IOnCollect, IOnDrop {

    private final int dmg = 20;
    private ItemData data;

    public Greatsword(){
        super(
            ItemType.Passive,
            AnimationBuilder.buildAnimation("item/world/Greatsword"),
            AnimationBuilder.buildAnimation("item/world/Greatsword"),
            "Greatsword",
            "Increases the owners damage by 20"
        );

        WorldItemBuilder.buildWorldItem(this);
        this.setOnCollect(this);
        this.setOnDrop(this);
    }

    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides){
        if(whoCollides instanceof Hero){
            Game.removeEntity(WorldItemEntity);
            Hero hero = (Hero) whoCollides;
            int currentDmg = hero.getDmg();
            hero.setDmg(currentDmg+dmg);
            System.out.println(hero.getDmg());

        }
    }

    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        if(user instanceof Hero){
            Hero hero = (Hero) user;
            int currentDmg = hero.getDmg();
            hero.setDmg(currentDmg-dmg);
            System.out.println();
        }
    }
}
