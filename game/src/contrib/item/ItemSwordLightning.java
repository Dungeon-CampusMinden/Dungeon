package contrib.item;

import core.utils.components.draw.Animation;

public class ItemSwordLightning extends Item {
    public ItemSwordLightning() {
        super(
                "Lightning Sword",
                "A sword that is made of pure lightning.",
                Animation.of("items/weapon/lightning_sword.png"));
    }
}
