package ecs.items;

import ecs.stats.Stats;
import graphic.Animation;

public abstract class ItemEquipment extends Item {

    private static final String DEFAULT_NAME = "Equipment Item";
    private static final String DEFAULT_DESCRIPTION = "This is an equipment item.";

    private Stats stats;

    public ItemEquipment() {
        super(
                ItemType.Equipment,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                DEFAULT_NAME,
                DEFAULT_DESCRIPTION);
        this.stats = new Stats();
    }

    public ItemEquipment(String name, String description) {
        super(
                ItemType.Equipment,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                name,
                description);
        this.stats = new Stats();
    }

    public ItemEquipment(String name, String description, Stats stats) {
        super(
                ItemType.Equipment,
                DEFAULT_INVENTORY_ANIMATION,
                DEFAULT_WORLD_ANIMATION,
                name,
                description);
        this.stats = stats;
    }

    public ItemEquipment(
            String name,
            String description,
            Stats stats,
            Animation inventoryTexture,
            Animation worldTexture) {
        super(ItemType.Equipment, inventoryTexture, worldTexture, name, description);
        this.stats = stats;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
