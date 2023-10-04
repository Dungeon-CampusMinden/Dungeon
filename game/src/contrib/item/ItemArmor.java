package contrib.item;

import core.utils.components.draw.Animation;

/** Abstract class representing an armor item. */
public abstract class ItemArmor extends Item {

    public enum ArmorType {
        HEAD,
        CHEST,
        LEGS,
        FOOT
    }

    private ArmorType type;

    protected ItemArmor(
            String displayName,
            String description,
            ArmorType type,
            Animation inventoryAnimation,
            Animation worldAnimation) {
        super(displayName, description, inventoryAnimation, worldAnimation);
        this.type = type;
    }

    /**
     * Get the type of the armor.
     *
     * @return the type of the armor.
     */
    public ArmorType type() {
        return this.type;
    }
}
