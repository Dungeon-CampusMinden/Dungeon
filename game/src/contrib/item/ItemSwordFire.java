package contrib.item;

import core.utils.components.draw.Animation;

public class ItemSwordFire extends Item {
    public ItemSwordFire() {
        super(
                "Fire Sword",
                "A sword that is on fire.",
                Animation.of("items/weapon/fire_sword.png"));
    }
}
