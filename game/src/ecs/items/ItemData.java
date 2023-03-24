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
import java.util.List;
import starter.Game;
import tools.Point;

public class ItemData {
    private ItemType itemType;
    private Animation inventoryTexture;
    private Animation worldTexture;
    private String itemName;
    private String description;

    public IOnCollect onCollect;
    public IOnDrop onDrop;
    // active
    public IOnUse onUse;

    // passive
    private DamageModifier damageModifier;

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
        this.onCollect = onCollect;
        this.onDrop = onDrop;
        this.onUse = onUse;
        this.damageModifier = damageModifier;
    }

    /**
     * creates a New Inventory item.
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

    public void triggerCollect(Entity worldItemEntity, Entity whoTriesCollects) {
        if (onCollect != null) onCollect.onCollect(worldItemEntity, whoTriesCollects);
    }

    /**
     * implements what should happen once the Item is dropped.
     *
     * @param position the location of the drop
     */
    public void triggerDrop(Entity e, Point position) {
        if (onDrop != null) onDrop.onDrop(e, this, position);
    }

    /**
     * Using active Item by calling associated callback.
     *
     * @param entity Entity that uses the item
     */
    public void triggerUse(Entity entity) {
        if (onUse == null) return;
        onUse.onUse(entity, this);
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
     * Default callback for item use. Prints a message to the console and removes the item from the
     * inventory.
     *
     * @param e Entity that uses the item
     * @param item Item that is used
     */
    private static void defaultUseCallback(Entity e, ItemData item) {
        e.getComponent(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            InventoryComponent invComp = (InventoryComponent) component;
                            invComp.removeItem(item);
                        });
        System.out.printf("Item \"%s\" used by entity %d\n", item.getItemName(), e.id);
    }

    private static void defaultDrop(Entity who, ItemData which, Point position) {
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, position);
        new AnimationComponent(droppedItem, which.getWorldTexture());
        HitboxComponent component = new HitboxComponent(droppedItem);
        component.setiCollideEnter((a, b, direction) -> which.triggerCollect(a, b));
    }

    private static void defaultCollect(Entity worldItem, Entity whoCollected) {
        Game.getHero()
                .ifPresent(
                        hero -> {
                            if (whoCollected.equals(hero)) {
                                hero.getComponent(InventoryComponent.class)
                                        .ifPresent(
                                                (x) -> {
                                                    if (((InventoryComponent) x)
                                                            .addItem(
                                                                    worldItem
                                                                            .getComponent(
                                                                                    ItemComponent
                                                                                            .class)
                                                                            .map(
                                                                                    ItemComponent
                                                                                                    .class
                                                                                            ::cast)
                                                                            .get()
                                                                            .itemData))
                                                        Game.removeEntity(worldItem);
                                                });
                            }
                        });
    }
}
