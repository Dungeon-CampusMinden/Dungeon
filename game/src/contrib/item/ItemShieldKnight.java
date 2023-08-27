package contrib.item;

import core.utils.components.draw.Animation;

public class ItemShieldKnight extends Item {
    public ItemShieldKnight() {
        super(
                "Knights Shield",
                "A shield that is used by knights.",
                Animation.of("items/shield/knight_shield.png"));
    }
}
