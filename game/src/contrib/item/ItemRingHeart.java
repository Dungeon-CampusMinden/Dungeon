package contrib.item;

import core.utils.components.draw.Animation;

public class ItemRingHeart extends Item {
    public ItemRingHeart() {
        super(
                "Heart Ring",
                "A ring with a heart on it.",
                Animation.of("items/ring/heart_ring.png"));
    }
}
