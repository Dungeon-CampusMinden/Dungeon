package contrib.item;

import core.utils.components.draw.Animation;

public class ItemPotionMana extends Item {
    public ItemPotionMana() {
        super(
                "Mana Potion",
                "A mana potion. It restores your mana.",
                Animation.of("items/potion/mana_potion.png"));
    }
}
