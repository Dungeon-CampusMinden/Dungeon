package ecs.items;

import configuration.ItemConfig;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.InventoryComponent;
import ecs.components.ItemComponent;
import ecs.components.PositionComponent;
import ecs.components.stats.DamageModifier;
import ecs.entities.Entity;
import graphic.Animation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import starter.Game;
import tools.Point;

/**
 * A Class which contains the Information of a specific Item.
 * It is used to create an ItemEntity which is used in the game.
 * This class is makes a difference between the ItemType Bag and the other ones.
 */
public class ItemData implements Serializable {
    private transient final Logger itemLogger = Logger.getLogger(this.getClass().getName());
    private List<ItemData> inventory;
    private ItemType itemType;
    private Animation inventoryTexture;
    private Animation worldTexture;
    private String itemName;
    private String description;

    private IOnCollect onCollect;
    private IOnDrop onDrop;
    // active
    private IOnUse onUse;

    // passive
    private DamageModifier damageModifier;

    /**
     * creates a new item data object.
     *
     * @param itemType
     * @param inventoryTexture
     * @param worldTexture
     * @param itemName
     * @param description
     * @param onCollect
     * @param onDrop
     * @param onUse
     * @param damageModifier
     *
     *                         IF ItemType.Bag is used this class creates a List
     *                         with 3 spaces, to save items init.
     */
    public ItemData(
            ItemType itemType,
            Animation inventoryTexture,
            Animation worldTexture,
            String itemName,
            String description,
            IOnCollect onCollect,
            IOnDrop onDrop,
            IOnUse onUse,
            DamageModifier damageModifier) {
        this.itemType = itemType;
        this.inventoryTexture = inventoryTexture;
        this.worldTexture = worldTexture;
        this.itemName = itemName;
        this.description = description;
        this.setOnCollect(onCollect);
        this.setOnDrop(onDrop);
        this.setOnUse(onUse);
        this.damageModifier = damageModifier;

        if (this.itemType.equals(ItemType.Bag)) {
            this.inventory = new ArrayList<>(3);
        }
        this.itemLogger.info("ItemData created");
    }

    /**
     * creates a new item data object. With a basic handling of collecting and
     * dropping
     *
     * @param itemType
     * @param inventoryTexture
     * @param worldTexture
     * @param itemName
     * @param description
     */
    public ItemData(
            ItemType itemType,
            Animation inventoryTexture,
            Animation worldTexture,
            String itemName,
            String description) {
        this(
                itemType,
                inventoryTexture,
                worldTexture,
                itemName,
                description,
                ItemData::defaultCollect,
                ItemData::defaultDrop,
                ItemData::defaultUseCallback,
                new DamageModifier());
    }

    public ItemData() {
        this(
                ItemConfig.TYPE.get(),
                new Animation(List.of(ItemConfig.TEXTURE.get()), 1),
                new Animation(List.of(ItemConfig.TEXTURE.get()), 1),
                ItemConfig.NAME.get(),
                ItemConfig.DESCRIPTION.get());
    }

    /**
     * what should happen when an Entity interacts with the Item while it is lying
     * in the World.
     *
     * @param worldItemEntity
     * @param whoTriesCollects
     */
    public void triggerCollect(Entity worldItemEntity, Entity whoTriesCollects) {
        itemLogger.info(worldItemEntity + " was collected by " + whoTriesCollects + "triggerCollect");
        if (getOnCollect() != null)
            getOnCollect().onCollect(worldItemEntity, whoTriesCollects);
    }

    /**
     * implements what should happen once the Item is dropped.
     *
     * @param position the location of the drop
     */
    public void triggerDrop(Entity e, Point position) {
        itemLogger.info(e + " was dropped at " + position + "triggerDrop");
        if (getOnDrop() != null)
            getOnDrop().onDrop(e, this, position);
    }

    /**
     * Using active Item by calling associated callback.
     *
     * @param entity Entity that uses the item
     */
    public void triggerUse(Entity entity) {
        itemLogger.info(entity + " used " + this + "triggerUse");
        if (getOnUse() == null)
            return;
        getOnUse().onUse(entity, this);
    }

    public ItemType getItemType() {
        return itemType;
    }

    public Animation getInventoryTexture() {
        return inventoryTexture;
    }

    public Animation getWorldTexture() {
        return worldTexture;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Default callback for item use. Prints a message to the console and removes
     * the item from the
     * inventory.
     *
     * @param e    Entity that uses the item
     * @param item Item that is used
     */
    private static void defaultUseCallback(Entity e, ItemData item) {
        item.itemLogger.info(e + " used " + item + "defaultUseCallback");
        if (!e.getComponent(InventoryComponent.class).isPresent())
            return;
        InventoryComponent ic = (InventoryComponent) e.getComponent(InventoryComponent.class).get();
        if (!item.getItemType().equals(ItemType.Bag)) {
            ic.removeItem(item);
            System.out.printf("Item \"%s\" used by entity %d\n", item.getItemName(), e.id);
            return;
        }
        if (item.getInventory().size() < 1) {
            System.out.println("Bag is empty");
            return;
        }
        System.out.printf("Item \"%s\" used by entity %d\n", item.getInventory().get(0).getItemName(), e.id);
        item.getInventory().remove(0);

    }

    /**
     * This methode is used to drop an item.
     * 
     * @param who      the entity, that drops the item
     * @param which    item that is dropped
     * @param position where the item will be dropped
     */
    private static void defaultDrop(Entity who, ItemData which, Point position) {
        which.itemLogger.info(who + " dropped " + which + "defaultDrop");
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, position);
        new AnimationComponent(droppedItem, which.getWorldTexture());
        HitboxComponent component = new HitboxComponent(droppedItem);
        component.setiCollideEnter((a, b, direction) -> which.triggerCollect(a, b));
        new ItemComponent(droppedItem, which);
    }

    /**
     * This methode is used to collect the item
     * 
     * @param worldItem    is the item, that will be collected
     * @param whoCollected that collects the item
     */
    private static void defaultCollect(Entity worldItem, Entity whoCollected) {
        if (!Game.getHero().isPresent())
            return;
        if (!whoCollected.equals(Game.getHero().get()))
            return;
        if (!whoCollected.getComponent(InventoryComponent.class).isPresent())
            return;
        InventoryComponent ic = (InventoryComponent) whoCollected.getComponent(InventoryComponent.class).get();
        if (ic.addItem(
                worldItem.getComponent(ItemComponent.class)
                        .map(ItemComponent.class::cast)
                        .get()
                        .getItemData()))
            Game.removeEntity(worldItem);
    }

    public IOnCollect getOnCollect() {
        return onCollect;
    }

    public void setOnCollect(IOnCollect onCollect) {
        this.onCollect = onCollect;
    }

    public IOnDrop getOnDrop() {
        return onDrop;
    }

    public void setOnDrop(IOnDrop onDrop) {
        this.onDrop = onDrop;
    }

    public IOnUse getOnUse() {
        return onUse;
    }

    public void setOnUse(IOnUse onUse) {
        this.onUse = onUse;
    }

    public List<ItemData> getInventory() {
        return this.inventory;
    }
}
