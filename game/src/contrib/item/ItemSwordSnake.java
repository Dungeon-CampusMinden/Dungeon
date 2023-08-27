package contrib.item;

import core.utils.components.draw.Animation;

public class ItemSwordSnake extends Item {
    public ItemSwordSnake() {
        super(
                "Snake Sword",
                "A sword that is made of a snake.",
                Animation.of("items/weapon/snake_sword.png"));
    }
}
