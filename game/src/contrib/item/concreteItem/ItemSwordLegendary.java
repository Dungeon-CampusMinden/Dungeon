package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemSwordLegendary extends Item {
    public ItemSwordLegendary() {
        super(
                "Legendary Sword",
                "A legendary sword. It's said that it was forged by the gods!",
                Animation.of("items/weapon/legendary_sword.png"));
    }
}
