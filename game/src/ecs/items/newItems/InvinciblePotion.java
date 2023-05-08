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

    //TODO: Need to fix bug where this function gets called on every levelLoad
    public void onUse(Entity e, ItemData item){
        HealthComponent hCp = null;


        //Set invincible if HealthComponent is present
        if(e.getComponent(HealthComponent.class).isPresent()){
            hCp = (HealthComponent) e.getComponent(HealthComponent.class).get();
        }

        if(hCp != null){
            hCp.setInvincible(true);
            System.out.println("Invincible for 5 seconds");


            //After 5 seconds, set invincible false
            Timer timer = new Timer();
            timer.schedule(new TimerTask(){
                    @Override
                    public void run() {
                        resetInvincible(e);
                    }
                }, (long) 5*1000);

        }
    }

    public void resetInvincible(Entity e){
        HealthComponent innerHCP;
        if(e.getComponent(HealthComponent.class).isPresent()){
            innerHCP = (HealthComponent) e.getComponent(HealthComponent.class).get();
            innerHCP.setInvincible(false);
        }
        System.out.println("Not longer invincible");
    }

}
