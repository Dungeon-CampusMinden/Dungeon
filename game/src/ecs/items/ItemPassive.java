package ecs.items;

import ecs.components.stats.DamageModifier;
import graphic.Animation;

public abstract class ItemPassive extends Item {

    private static final String DEFAULT_NAME = "Equipment Item";
    private static final String DEFAULT_DESCRIPTION = "This is an equipment item.";

    private DamageModifier damageModifier;

    /** Create new passive Item with default values. */
    public ItemPassive() {
        this(DEFAULT_NAME, DEFAULT_DESCRIPTION);
    }

    /**
     * Create new passive Item with given name and description.
     *
     * @param name Name of the item
     * @param description Description of the item
     */
    public ItemPassive(String name, String description) {
        this(name, description, new DamageModifier());
    }

    /**
     * Create new passive Item with given name, description and DamageModifier.
     *
     * @param name Name of the item
     * @param description Description of the item
     * @param damageModifier {@link ecs.components.stats.DamageModifier} of the item
     */
    public ItemPassive(String name, String description, DamageModifier damageModifier) {
        this(
                name,
                description,
                damageModifier,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION);
    }

    /**
     * Create new passive Item with given name, description, DamageModifier and textures.
     *
     * @param name Name of the item
     * @param description Description of the item
     * @param damageModifier {@link ecs.components.stats.DamageModifier} of the item
     * @param inventoryTexture Texture of the item in the inventory
     * @param worldTexture Texture of the item in the world
     */
    public ItemPassive(
            String name,
            String description,
            DamageModifier damageModifier,
            Animation inventoryTexture,
            Animation worldTexture) {
        super(ItemType.Passive, inventoryTexture, worldTexture, name, description);
        this.damageModifier = damageModifier;
    }

    /**
     * Get the DamageModifier of the item.
     *
     * @return {@link ecs.components.stats.DamageModifier} of the item
     */
    public DamageModifier getDamageModifier() {
        return damageModifier;
    }

    /**
     * Set the DamageModifier of the item.
     *
     * @param damageModifier {@link ecs.components.stats.DamageModifier} of the item
     */
    public void setDamageModifier(DamageModifier damageModifier) {
        this.damageModifier = damageModifier;
    }
}
