package contrib.utils.components.item;

import contrib.components.CollideComponent;
import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import contrib.configuration.ItemConfig;
import contrib.utils.components.stats.DamageModifier;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;
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
public final class ItemData {
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
            final ItemType itemType,
            final Animation inventoryTexture,
            final Animation worldTexture,
            final String itemName,
            final String description,
            final BiConsumer<Entity, Entity> onCollect,
            final TriConsumer<Entity, ItemData, Point> onDrop,
            final BiConsumer<Entity, ItemData> onUse,
            final DamageModifier damageModifier) {
        this.itemType = itemType;
        this.inventoryTexture = inventoryTexture;
        this.worldTexture = worldTexture;
        this.itemName = itemName;
        this.description = description;
        this.onCollect(onCollect);
        this.onDrop(onDrop);
        this.onUse(onUse);
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
            final ItemType itemType,
            final Animation inventoryTexture,
            final Animation worldTexture,
            final String itemName,
            final String description) {
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
                ItemConfig.TYPE.value(),
                new Animation(List.of(ItemConfig.TEXTURE.value()), 1),
                new Animation(List.of(ItemConfig.TEXTURE.value()), 1),
                ItemConfig.NAME.value(),
                ItemConfig.DESCRIPTION.value());
    }

    /**
     * what should happen when an Entity interacts with the Item while it is lying in the World.
     *
     * @param worldItemEntity Item which is collected
     * @param whoTriesCollects Entity that tries to collect item
     */
    public void triggerCollect(final Entity worldItemEntity, final Entity whoTriesCollects) {
        if (onCollect() != null) onCollect().accept(worldItemEntity, whoTriesCollects);
    }

    /**
     * implements what should happen once the Item is dropped.
     *
     * @param position the location of the drop
     */
    public void triggerDrop(final Entity e, final Point position) {
        if (onDrop() != null) onDrop().accept(e, this, position);
    }

    /**
     * Using active Item by calling associated callback.
     *
     * @param entity Entity that uses the item
     */
    public void triggerUse(final Entity entity) {
        if (onUse() == null) return;
        onUse().accept(entity, this);
    }

    /**
     * @return The current itemType.
     */
    public ItemType itemType() {
        return itemType;
    }

    /**
     * @return The current inventory animation
     */
    public Animation inventoryTexture() {
        return inventoryTexture;
    }

    /**
     * @return The current world animation
     */
    public Animation worldTexture() {
        return worldTexture;
    }

    /**
     * @return The current item name.
     */
    public String itemName() {
        return itemName;
    }

    /**
     * @return The current item description.
     */
    public String description() {
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
        e.fetch(InventoryComponent.class).ifPresent(component -> component.removeItem(item));
        System.out.printf("Item \"%s\" used by entity %d\n", item.itemName(), e.id());
    }

    /**
     * Default callback for dropping item.
     *
     * @param who Entity dropping the item.
     * @param which Item that is being dropped.
     * @param position Position where to drop the item.
     */
    private static void defaultDrop(Entity who, ItemData which, Point position) {
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, position);
        new DrawComponent(droppedItem, which.worldTexture());
        CollideComponent component = new CollideComponent(droppedItem);
        component.collideEnter((a, b, direction) -> which.triggerCollect(a, b));
    }

    /**
     * Default callback for collecting items.
     *
     * @param worldItem Item in world that is being collected.
     * @param whoCollected Entity that tries to pick up item.
     */
    private static void defaultCollect(Entity worldItem, Entity whoCollected) {
        // check if the Game has a Hero
        Game.hero()
                .ifPresent(
                        hero -> {
                            // check if entity picking up Item is the Hero
                            if (whoCollected.equals(hero)) {
                                // check if Hero has an Inventory Component
                                hero.fetch(InventoryComponent.class)
                                        .ifPresent(
                                                (x) -> {
                                                    // check if Item can be added to hero Inventory
                                                    if ((x)
                                                            .addItem(
                                                                    worldItem
                                                                            .fetch(
                                                                                    ItemComponent
                                                                                            .class)
                                                                            .orElseThrow(
                                                                                    () ->
                                                                                            MissingComponentException
                                                                                                    .build(
                                                                                                            worldItem,
                                                                                                            ItemComponent
                                                                                                                    .class))
                                                                            .itemData()))
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
    public BiConsumer<Entity, Entity> onCollect() {
        return onCollect;
    }

    /**
     * Set the callback function to collect the item.
     *
     * @param onCollect New collect callback.
     */
    public void onCollect(BiConsumer<Entity, Entity> onCollect) {
        this.onCollect = onCollect;
    }

    /**
     * @return The callback function to drop the item.
     */
    public TriConsumer<Entity, ItemData, Point> onDrop() {
        return onDrop;
    }

    /**
     * Set the callback function to drop the item.
     *
     * @param onDrop New drop callback.
     */
    public void onDrop(TriConsumer<Entity, ItemData, Point> onDrop) {
        this.onDrop = onDrop;
    }

    /**
     * @return The callback function to use the item.
     */
    public BiConsumer<Entity, ItemData> onUse() {
        return onUse;
    }

    /**
     * Set the callback function to use the item.
     *
     * @param onUse New use callback.
     */
    public void onUse(BiConsumer<Entity, ItemData> onUse) {
        this.onUse = onUse;
    }
}
