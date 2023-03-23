package ecs.items;

import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.InventoryComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.ArrayList;
import java.util.List;
import starter.Game;
import tools.Point;

public class ItemData {

    public static final List<ItemData> ITEM_DATA_REGISTER = new ArrayList<>();
    public static final List<String> missingTexture = List.of("animation/missingTexture.png");

    public static final String DEFAULT_DESCRIPTION = "Default Description.";
    public static final ItemType DEFAULT_ITEM_TYPE = ItemType.Basic;
    public static final String DEFAULT_NAME = "Default_name";
    public static final Animation DEFAULT_WORLD_ANIMATION = new Animation(missingTexture, 1);
    public static final Animation DEFAULT_INVENTORY_ANIMATION = new Animation(missingTexture, 1);

    private ItemType itemType;
    private Animation inventoryTexture;
    private Animation worldTexture;
    private String itemName;
    private String description;

    private IOnCollect onCollect;
    private IOnDrop onDrop;
    private IOnUse onUse;

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
        this.itemType = itemType;
        this.inventoryTexture = inventoryTexture;
        this.worldTexture = worldTexture;
        this.itemName = itemName;
        this.description = description;
    }

    public ItemData() {
        this(
                DEFAULT_ITEM_TYPE,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION);
    }

    /**
     * implements what should happen once the Item is dropped.
     *
     * @param position the location of the drop
     */
    public void triggerDrop(Entity e, Point position) {
        onDrop.onDrop(e, this, position);
    }

    private static void defaultDrop(Entity who, ItemData which, Point position) {
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, position);
        new AnimationComponent(droppedItem, which.worldTexture);
        HitboxComponent component = new HitboxComponent(droppedItem);
        component.setiCollideEnter(
                (a, b, direction) -> {
                    Game.getHero()
                            .ifPresent(
                                    hero -> {
                                        if (b.equals(hero)) {
                                            hero.getComponent(InventoryComponent.class)
                                                    .ifPresent(
                                                            (x) -> {
                                                                if (((InventoryComponent) x)
                                                                        .addItem(which))
                                                                    Game.removeEntity(droppedItem);
                                                            });
                                        }
                                    });
                });
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
}
