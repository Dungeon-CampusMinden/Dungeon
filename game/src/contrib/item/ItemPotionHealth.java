package contrib.item;

import core.utils.components.draw.Animation;

public class ItemPotionHealth extends Item {
    public ItemPotionHealth() {
        super(
                "Health Potion",
                "A health potion. It heals you several health points.",
                Animation.of("items/potion/health_potion.png"));
    }
}
