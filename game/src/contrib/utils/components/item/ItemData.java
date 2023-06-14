package contrib.utils.components.item;

import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import contrib.configuration.ItemConfig;
import contrib.entities.WorldItemBuilder;
import contrib.utils.components.stats.DamageModifier;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A Class which contains the Information of a specific Item.
 *
 * <p>It holds the method references for collecting ({@link #onCollect}), dropping ({@link #onDrop})
 * and using ({@link #onUse}) items as functional Interfaces.
 *
 * <p>Lastly it holds a {@link #damageModifier}
 */
public class ItemData {

    private Item item;
    private BiConsumer<Entity, Entity> onCollect;
    private TriConsumer<Entity, ItemData, Point> onDrop;
    // active
    private BiConsumer<Entity, ItemData> onUse;

    // passive
    private final DamageModifier damageModifier;

    /**
     * creates a new item data object.
     *
     * @param item Enum entry describing item.
     * @param onCollect Functional interface defining behaviour when item is collected.
     * @param onDrop Functional interface defining behaviour when item is dropped.
     * @param onUse Functional interface defining behaviour when item is used.
     * @param damageModifier Defining if dealt damage is altered.
     */
    public ItemData(
            Item item,
            BiConsumer<Entity, Entity> onCollect,
            TriConsumer<Entity, ItemData, Point> onDrop,
            BiConsumer<Entity, ItemData> onUse,
            DamageModifier damageModifier) {
        this.item = item;
        this.setOnCollect(onCollect);
        this.setOnDrop(onDrop);
        this.setOnUse(onUse);
        this.damageModifier = damageModifier;
    }

    /**
     * creates a new item data object. With a basic handling of collecting, dropping and using.
     *
     * @param item Enum entry describing item.
     */
    public ItemData(Item item) {
        this(
                item,
                ItemData::defaultCollect,
                ItemData::defaultDrop,
                ItemData::defaultUseCallback,
                new DamageModifier());
    }

    /** Constructing object with completely default values. Taken from {@link ItemConfig}. */
    public ItemData() {
        this(Item.valueOf(ItemConfig.DEFAULT_ITEM.get()));
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
     * @return Get the current Item
     */
    public Item getItem() {
        return this.item;
    }

    /**
     * Default callback for item use. Prints a message to the console and removes the item from the
     * inventory.
     *
     * @param e Entity that uses the item
     * @param itemData Item that is used
     */
    private static void defaultUseCallback(Entity e, ItemData itemData) {
        e.getComponent(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            InventoryComponent invComp = (InventoryComponent) component;
                            invComp.removeItem(itemData);
                        });
        System.out.printf("Item \"%s\" used by entity %d\n", itemData.getItem().getName(), e.id());
    }

    /**
     * Default callback for dropping item.
     *
     * @param who Entity dropping the item.
     * @param which Item that is being dropped.
     * @param position Position where to drop the item.
     */
    private static void defaultDrop(Entity who, ItemData which, Point position) {
        Entity droppedItem = WorldItemBuilder.buildWorldItem(which);
        droppedItem
                .getComponent(PositionComponent.class)
                .map(PositionComponent.class::cast)
                .ifPresent(x -> x.position(position));
    }

    /**
     * Default callback for collecting items.
     *
     * @param worldItem Item in world that is being collected.
     * @param whoCollected Entity that tries to pick up item.
     */
    private static void defaultCollect(Entity worldItem, Entity whoCollected) {
        // check if the Game has a Hero

        Optional<ItemComponent> itemComp =
                worldItem.getComponent(ItemComponent.class).map(ItemComponent.class::cast);
        if (itemComp.isEmpty()) return;

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
                                                    if (((InventoryComponent) x)
                                                            .addItem(itemComp.get().getItemData()))
                                                        // if added to hero Inventory
                                                        // remove Item from World
                                                        Game.removeEntity(worldItem);
                                                    System.out.println("Item collected");
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
