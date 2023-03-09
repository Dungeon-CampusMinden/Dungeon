package ecs.items;

import ecs.components.stats.Stats;
import graphic.Animation;

public abstract class ItemPassive extends Item {

    private static final String DEFAULT_NAME = "Equipment Item";
    private static final String DEFAULT_DESCRIPTION = "This is an equipment item.";

    private Stats stats;

    public ItemPassive() {
        super(
                ItemType.Passive,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION);
        this.stats = new Stats();
    }

    public ItemPassive(String name, String description) {
        super(
                ItemType.Passive,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                name,
                description);
        this.stats = new Stats();
    }

    public ItemPassive(String name, String description, Stats stats) {
        super(
                ItemType.Passive,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                name,
                description);
        this.stats = stats;
    }

    public ItemPassive(
            String name,
            String description,
            Stats stats,
            Animation inventoryTexture,
            Animation worldTexture) {
        super(ItemType.Passive, inventoryTexture, worldTexture, name, description);
        this.stats = stats;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
