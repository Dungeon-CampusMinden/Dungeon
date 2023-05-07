package ecs.items.newItems;
import dslToGame.AnimationBuilder;
import ecs.components.HealthComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.graphic.Animation;
import ecs.items.IOnUse;
import ecs.items.ItemData;
import ecs.items.ItemType;
import ecs.items.WorldItemBuilder;
import starter.Game;

import java.util.Timer;
import java.util.TimerTask;


public class InvinciblePotion extends ItemData implements IOnUse {

    private String world = "item/world/InvinciblePotion";
    private String inv = "item/world/InvinciblePotion";
    private final String name = "Invincible Potion";
    private final String description = "A Potion which makes you imortal to damage for 5 seconds";
    ItemType active = ItemType.Active;
    Animation worldAnim = AnimationBuilder.buildAnimation(world);
    Animation invAnim = AnimationBuilder.buildAnimation(inv);

    public InvinciblePotion(){
        WorldItemBuilder.buildWorldItem(new ItemData(active,worldAnim,invAnim,name,description));
        onUse(Game.getHero().get() ,this);
    }

    public void onUse(Entity e, ItemData item){

        long startTime = System.currentTimeMillis();
        Timer timer = new Timer();
        HealthComponent ofE = (HealthComponent) e.getComponent(HealthComponent.class).get();
        int saveHealth = ofE.getCurrentHealthpoints();
        ofE.setCurrentHealthpoints(10000);
        System.out.println("imortal for 5 seconds");
        timer.schedule(new TimerTask() {
            public void run() {
                ofE.setCurrentHealthpoints(saveHealth);
                System.out.println("Not longer imortal");
            }
        }, 5000);

    }

}
