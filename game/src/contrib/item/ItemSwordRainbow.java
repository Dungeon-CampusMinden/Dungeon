package contrib.item;

import core.utils.components.draw.Animation;

public class ItemSwordRainbow extends Item {
    public ItemSwordRainbow() {
        super(
                "Rainbow Sword",
                "A sword that is made of a rainbow. Maybe it leads you to a pot of gold?",
                Animation.of("items/weapon/rainbow_sword.png"));
    }
}
