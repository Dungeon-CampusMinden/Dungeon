package ecs.items;

import ecs.components.stats.Stats;
import graphic.Animation;

public abstract class ItemPassive extends Item {

    private static final String DEFAULT_NAME = "Equipment Item";
    private static final String DEFAULT_DESCRIPTION = "This is an equipment item.";

    private Stats stats;

    /** Create new passive Item with default values. */
    public ItemPassive() {
        super(
                ItemType.Passive,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION);
        this.stats = new Stats();
    }

    /**
     * Create new passive Item with given name and description.
     *
     * @param name Name of the item
     * @param description Description of the item
     */
    public ItemPassive(String name, String description) {
        super(
                ItemType.Passive,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                name,
                description);
        this.stats = new Stats();
    }

    /**
     * Create new passive Item with given name, description and stats.
     *
     * @param name Name of the item
     * @param description Description of the item
     * @param stats Stats of the item
     */
    public ItemPassive(String name, String description, Stats stats) {
        super(
                ItemType.Passive,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                name,
                description);
        this.stats = stats;
    }

    /**
     * Create new passive Item with given name, description, stats and textures.
     *
     * @param name Name of the item
     * @param description Description of the item
     * @param stats Stats of the item
     * @param inventoryTexture Texture of the item in the inventory
     * @param worldTexture Texture of the item in the world
     */
    public ItemPassive(
            String name,
            String description,
            Stats stats,
            Animation inventoryTexture,
            Animation worldTexture) {
        super(ItemType.Passive, inventoryTexture, worldTexture, name, description);
        this.stats = stats;
    }

    /**
     * Get the stats of the item.
     *
     * @return Stats of the item
     */
    public Stats getStats() {
        return stats;
    }

    /**
     * Set the stats of the item.
     *
     * @param stats Stats of the item
     */
    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
