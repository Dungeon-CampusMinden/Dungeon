package contrib.item;

import core.utils.components.draw.Animation;

public class ItemPotionWater extends Item {
    public ItemPotionWater() {
        super(
                "Bottle of Water",
                "A bottle of water. It's not very useful except for hydration.",
                Animation.of("items/potion/water_bottle.png"));
    }
}
