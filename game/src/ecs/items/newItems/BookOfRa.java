package ecs.items.newItems;

import dslToGame.AnimationBuilder;
import ecs.components.InventoryComponent;
import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.items.IOnCollect;
import ecs.items.ItemData;
import ecs.items.ItemType;
import ecs.items.WorldItemBuilder;
import java.util.Random;
import starter.Game;

/** Book of Ra grants the hero 1-5% XP of the XP left to level up per book in inventory. */
public class BookOfRa extends ItemData implements IOnCollect {
    InventoryComponent inv;
    Hero hero;

    public BookOfRa() {
        super(
                ItemType.Passive,
                AnimationBuilder.buildAnimation("item/world/BookOfRa"),
                AnimationBuilder.buildAnimation("item/world/BookOfRa"),
                "Book of Ra",
                "Gives the owner a bonus EP of 1-5% every time he enters a new level");

        hero = null;
        if (Game.getHero().isPresent()) {
            hero = (Hero) Game.getHero().get();
        }

        if (hero.getComponent(InventoryComponent.class).isPresent()) {
            inv = (hero.getInv());
        }

        WorldItemBuilder.buildWorldItem(this);

        this.setOnCollect(this);
    }

    /**
     * Adds item to the inventory or if available to a book bag
     *
     * @param WorldItemEntity
     * @param whoCollides
     */
    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        if (whoCollides instanceof Hero) {
            for (ItemData item : inv.getItems()) {
                if (item instanceof Bag bag) {
                    if (bag.addItem(this)) {
                        Game.removeEntity(WorldItemEntity);
                        itemLogger.info(this.getItemName() + " has been added to the Book Bag.");
                        return;
                    }
                }
            }

            if (inv.addItem(this)) {
                Game.removeEntity(WorldItemEntity);
                itemLogger.info(this.getItemName() + " has been added to the Inventory");
            } else {
                itemLogger.info("No space for item in inventory.");
            }
        }
    }

    /** Rewarding function that grants the hero xp. Is called in onLevelLoad() */
    public void grantXP() {
        XPComponent xp = null;

        if (hero.getComponent(XPComponent.class).isPresent()) {
            xp = (XPComponent) hero.getComponent(XPComponent.class).get();
        }

        Random random = new Random();
        int randomXP = (int) ((xp.getXPToNextLevel() * random.nextInt(1, 5)) / 100);
        xp.addXP(randomXP);
        itemLogger.info("XP missing to level up: " + xp.getXPToNextLevel());
        itemLogger.info("Granted " + randomXP + " by the Book of Ra.");
    }
}
