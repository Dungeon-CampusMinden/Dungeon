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

import java.util.Optional;
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

    private Hero hero;

    public InvinciblePotion(Hero hero) {
        this.hero = hero;
        WorldItemBuilder.buildWorldItem(new ItemData(active,worldAnim,invAnim,name,description));
        onUse(hero,this);
    }

    public void onUse(Entity e, ItemData item){

        long startTime = System.currentTimeMillis();
        Timer timer = new Timer();
        int resetHealth = this.hero.getCurrentHealth();
        this.hero.setHealth(100);
        System.out.println("imortal for 5 seconds");
        timer.schedule(new TimerTask() {

            public void run() {
               hero.setCurrentHealth(resetHealth);
                System.out.println(hero.getCurrentHealth());
                System.out.println("Not longer imortal");
            }
        }, 5*1000);

    }

}
