package ecs.items;

import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import graphic.Animation;

public abstract class ItemActive extends Item {

    private static final String DEFAULT_NAME = "Usable Item";
    private static final String DEFAULT_DESCRIPTION = "This is a usable item.";

    private final IItemUse callbackItemUse;

    /** Creates a new usable Item with default values. */
    public ItemActive() {
        this(
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                ItemActive::defaultUseCallback);
    }

    /**
     * Creates a new usable Item with the given name and description. The textures are set to the
     * default
     *
     * @param name Name of the item
     * @param description Description of the item
     */
    public ItemActive(String name, String description) {
        this(
                name,
                description,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                ItemActive::defaultUseCallback);
    }

    /**
     * Creates a new usable Item with the given name, description and textures.
     *
     * @param name Name of the item
     * @param description Description of the item
     * @param inventoryTexture Texture of the item in the inventory
     * @param worldTexture Texture of the item in the world
     */
    public ItemActive(
            String name, String description, Animation inventoryTexture, Animation worldTexture) {
        this(name, description, inventoryTexture, worldTexture, ItemActive::defaultUseCallback);
    }

    /**
     * Creates a new usable Item with the given name, description, textures and callback.
     *
     * @param name Name of the item
     * @param description Description of the item
     * @param inventoryTexture Texture of the item in the inventory
     * @param worldTexture Texture of the item in the world
     * @param callback Callback to be called when the item is used
     */
    public ItemActive(
            String name,
            String description,
            Animation inventoryTexture,
            Animation worldTexture,
            IItemUse callback) {
        super(ItemType.Active, inventoryTexture, worldTexture, name, description);
        callbackItemUse = callback;
    }

    /**
     * Using active Item by calling associated callback.
     *
     * @param entity Entity that uses the item
     */
    public void use(Entity entity) {
        callbackItemUse.onUse(entity, this);
    }

    /**
     * Default callback for item use. Prints a message to the console and removes the item from the
     * inventory.
     *
     * @param e Entity that uses the item
     * @param item Item that is used
     */
    private static void defaultUseCallback(Entity e, ItemActive item) {
        e.getComponent(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            InventoryComponent invComp = (InventoryComponent) component;
                            invComp.removeItem(item);
                        });
        System.out.printf("Item \"%s\" used by entity %d\n", item.getItemName(), e.id);
    }
}
