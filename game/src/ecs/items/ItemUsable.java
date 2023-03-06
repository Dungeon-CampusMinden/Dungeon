package ecs.items;

import ecs.entities.Entity;
import graphic.Animation;

public abstract class ItemUsable extends Item {

    private static final String DEFAULT_NAME = "Usable Item";
    private static final String DEFAULT_DESCRIPTION = "This is a usable item.";

    private IItemUse callbackItemUse;

    /** Creates a new usable Item with default values. */
    public ItemUsable() {
        this(
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                ItemUsable::defaultUseCallback);
    }

    /**
     * Creates a new usable Item with the given name and description. The textures are set to the
     * default
     *
     * @param name Name of the item
     * @param description Description of the item
     */
    public ItemUsable(String name, String description) {
        this(
                name,
                description,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                ItemUsable::defaultUseCallback);
    }

    /**
     * Creates a new usable Item with the given name, description and textures.
     *
     * @param name Name of the item
     * @param description Description of the item
     * @param inventoryTexture Texture of the item in the inventory
     * @param worldTexture Texture of the item in the world
     */
    public ItemUsable(
            String name, String description, Animation inventoryTexture, Animation worldTexture) {
        this(name, description, inventoryTexture, worldTexture, ItemUsable::defaultUseCallback);
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
    public ItemUsable(
            String name,
            String description,
            Animation inventoryTexture,
            Animation worldTexture,
            IItemUse callback) {
        super(ItemType.Usable, inventoryTexture, worldTexture, name, description);
        callbackItemUse = callback;
    }

    /**
     * Use item
     *
     * @param entity Entity that uses the item
     */
    public void use(Entity entity) {
        callbackItemUse.onUse(entity, this);
    }

    private static void defaultUseCallback(Entity e, ItemUsable item) {
        System.out.printf("Item \"%s\" used by entity %d\n", item.getItemName(), e.id);
    }
}
