package contrib.item;

import core.utils.components.draw.Animation;

public class ItemSwordIce extends Item {
    public ItemSwordIce() {
        super(
                "Ice Sword",
                "A sword that is made of ice. Be careful, it's really cold!",
                Animation.of("items/weapon/ice_sword.png"));
    }
}
