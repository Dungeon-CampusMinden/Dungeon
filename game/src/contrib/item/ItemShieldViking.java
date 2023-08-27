package contrib.item;

import core.utils.components.draw.Animation;

public class ItemShieldViking extends Item {
    public ItemShieldViking() {
        super(
                "Vikings Shield",
                "A Shield that is used by vikings.",
                Animation.of("items/shield/viking_shield.png"));
    }
}
