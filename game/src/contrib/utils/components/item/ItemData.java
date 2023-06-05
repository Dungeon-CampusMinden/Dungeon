package contrib.utils.components.item;

import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import contrib.configuration.ItemConfig;
import contrib.entities.WorldItemBuilder;
import contrib.utils.components.stats.DamageModifier;

import core.Entity;
import core.Game;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.Animation;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A Class which contains the Information of a specific Item.
 *
 * <p>It contains the {@link #itemType}, animations / textures for inside the hero inventory ({@link
 * #inventoryTexture}) or in the world ({@link #worldTexture}), as well as the {@link #itemName} and
 * a {@link #description}.
 *
 * <p>It holds the method references for collecting ({@link #onCollect}), dropping ({@link #onDrop})
 * and using ({@link #onUse}) items as functional Interfaces.
 *
 * <p>Lastly it holds a {@link #damageModifier}
 */
public class ItemData {
    private final ItemType itemType;
    private final Animation inventoryTexture;
    private final Animation worldTexture;
    private final String itemName;
    private final String description;

    private BiConsumer<Entity, Entity> onCollect;
    private TriConsumer<Entity, ItemData, Point> onDrop;
    // active
    private BiConsumer<Entity, ItemData> onUse;

    // passive
    private final DamageModifier damageModifier;

    /**
     * creates a new item data object.
     *
     * @param itemType Enum entry describing item type.
     * @param inventoryTexture Animation that is played inside the hero inventory.
     * @param worldTexture Animation that is played while item is dropped in the world.
     * @param itemName String defining name of item.
     * @param description String giving a description of the item
     * @param onCollect Functional interface defining behaviour when item is collected.
     * @param onDrop Functional interface defining behaviour when item is dropped.
     * @param onUse Functional interface defining behaviour when item is used.
     * @param damageModifier Defining if dealt damage is altered.
     */
    public ItemData(
            ItemType itemType,
            Animation inventoryTexture,
            Animation worldTexture,
            String itemName,
            String description,
            BiConsumer<Entity, Entity> onCollect,
            TriConsumer<Entity, ItemData, Point> onDrop,
            BiConsumer<Entity, ItemData> onUse,
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
    }

    /**
     * creates a new item data object. With a basic handling of collecting, dropping and using.
     *
     * @param itemType Enum entry describing item type.
     * @param inventoryTexture Animation that is played inside the hero inventory.
     * @param worldTexture Animation that is played while item is dropped in the world.
     * @param itemName String defining name of item.
     * @param description String giving a description of the item
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

    /** Constructing object with completely default values. Taken from {@link ItemConfig}. */
    public ItemData() {
        this(
                ItemConfig.TYPE.get(),
                new Animation(List.of(ItemConfig.TEXTURE.get()), 1),
                new Animation(List.of(ItemConfig.TEXTURE.get()), 1),
                ItemConfig.NAME.get(),
                ItemConfig.DESCRIPTION.get());
    }

    /**
     * what should happen when an Entity interacts with the Item while it is lying in the World.
     *
     * @param worldItemEntity Item which is collected
     * @param whoTriesCollects Entity that tries to collect item
     */
    public void triggerCollect(Entity worldItemEntity, Entity whoTriesCollects) {
        if (getOnCollect() != null) getOnCollect().accept(worldItemEntity, whoTriesCollects);
    }

    /**
     * implements what should happen once the Item is dropped.
     *
     * @param position the location of the drop
     */
    public void triggerDrop(Entity e, Point position) {
        if (getOnDrop() != null) getOnDrop().accept(e, this, position);
    }

    /**
     * Using active Item by calling associated callback.
     *
     * @param entity Entity that uses the item
     */
    public void triggerUse(Entity entity) {
        if (getOnUse() == null) return;
        getOnUse().accept(entity, this);
    }

    /**
     * @return The current itemType.
     */
    public ItemType getItemType() {
        return itemType;
    }

    /**
     * @return The current inventory animation
     */
    public Animation getInventoryTexture() {
        return inventoryTexture;
    }

    /**
     * @return The current world animation
     */
    public Animation getWorldTexture() {
        return worldTexture;
    }

    /**
     * @return The current item name.
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @return The current item description.
     */
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
        System.out.printf("Item \"%s\" used by entity %d\n", item.getItemName(), e.id());
    }

    /**
     * Default callback for dropping item.
     *
     * @param who Entity dropping the item.
     * @param which Item that is being dropped.
     * @param position Position where to drop the item.
     */
    private static void defaultDrop(Entity who, ItemData which, Point position) {
        InventoryComponent inventoryComponent =
                (InventoryComponent) who.getComponent(InventoryComponent.class).orElseThrow();
        inventoryComponent.removeItem(which);
        WorldItemBuilder.buildWorldItem(which, position);
    }

    /**
     * Default callback for collecting items.
     *
     * @param worldItem Item in world that is being collected.
     * @param whoCollected Entity that tries to pick up item.
     */
    private static void defaultCollect(Entity worldItem, Entity whoCollected) {
        // check if the Game has a Hero
        Game.getHero()
                .ifPresent(
                        hero -> {
                            // check if entity picking up Item is the Hero
                            if (whoCollected.equals(hero)) {
                                // check if Hero has an Inventory Component
                                hero.getComponent(InventoryComponent.class)
                                        .ifPresent(
                                                (x) -> {
                                                    // check if Item can be added to hero Inventory
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
                                                                            .getItemData()))
                                                        // if added to hero Inventory
                                                        // remove Item from World
                                                        Game.removeEntity(worldItem);
                                                });
                            }
                        });
    }

    /**
     * @return The callback function to collect the item.
     */
    public BiConsumer<Entity, Entity> getOnCollect() {
        return onCollect;
    }

    /**
     * Set the callback function to collect the item.
     *
     * @param onCollect New collect callback.
     */
    public void setOnCollect(BiConsumer<Entity, Entity> onCollect) {
        this.onCollect = onCollect;
    }

    /**
     * @return The callback function to drop the item.
     */
    public TriConsumer<Entity, ItemData, Point> getOnDrop() {
        return onDrop;
    }

    /**
     * Set the callback function to drop the item.
     *
     * @param onDrop New drop callback.
     */
    public void setOnDrop(TriConsumer<Entity, ItemData, Point> onDrop) {
        this.onDrop = onDrop;
    }

    /**
     * @return The callback function to use the item.
     */
    public BiConsumer<Entity, ItemData> getOnUse() {
        return onUse;
    }

    /**
     * Set the callback function to use the item.
     *
     * @param onUse New use callback.
     */
    public void setOnUse(BiConsumer<Entity, ItemData> onUse) {
        this.onUse = onUse;
    }
}
