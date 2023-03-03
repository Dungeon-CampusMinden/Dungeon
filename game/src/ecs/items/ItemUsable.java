package ecs.items;

import graphic.Animation;

public abstract class ItemUsable extends Item {

    private static final String DEFAULT_NAME = "Usable Item";
    private static final String DEFAULT_DESCRIPTION = "This is a usable item.";

    /** Creates a new usable Item with default values. */
    public ItemUsable() {
        super(
                ItemType.Usable,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION);
    }

    /**
     * Creates a new usable Item with the given name and description. The textures are set to the
     * default
     *
     * @param name Name of the item
     * @param description Description of the item
     */
    public ItemUsable(String name, String description) {
        super(
                ItemType.Usable,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                name,
                description);
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
        super(ItemType.Usable, inventoryTexture, worldTexture, name, description);
    }

    /** Implements what should happen ones the Item is used. */
    public abstract void use();
}
