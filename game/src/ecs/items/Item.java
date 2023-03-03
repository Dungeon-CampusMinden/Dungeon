package ecs.items;

import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import mydungeon.ECS;
import tools.Point;

public abstract class Item {
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

    /**
     * creates a New Inventory item.
     *
     * @param itemType Item type
     * @param inventoryTexture Texture of an inventar
     * @param worldTexture Texture of the world
     * @param itemName an items name
     * @param description description
     */
    public Item(
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

    public Item() {
        this(
                DEFAULT_ITEM_TYPE,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION);
    }

    /**
     * implements what should happen ones the Item is dropped.
     *
     * @param position the location of the drop
     */
    public void onDrop(Point position) {
        Entity droppedItem = new Entity();
        new PositionComponent(droppedItem, position);
        new AnimationComponent(droppedItem, worldTexture);
        HitboxComponent component = new HitboxComponent(droppedItem);
        component.setCollideMethod(
                (a, b, direction) -> {
                    if (b.equals(ECS.hero)) {
                        System.out.println("add item to inventory");
                        ECS.entitiesToRemove.add(droppedItem);
                    }
                });

        ECS.entities.add(droppedItem);
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Animation getInventoryTexture() {
        return inventoryTexture;
    }

    public void setInventoryTexture(Animation inventoryTexture) {
        this.inventoryTexture = inventoryTexture;
    }

    public Animation getWorldTexture() {
        return worldTexture;
    }

    public void setWorldTexture(Animation worldTexture) {
        this.worldTexture = worldTexture;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
