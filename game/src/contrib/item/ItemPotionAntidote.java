package contrib.item;

import core.utils.components.draw.Animation;

public class ItemPotionAntidote extends Item {
    public ItemPotionAntidote() {
        super(
                "Antidote",
                "An antidote. It cures poison.",
                Animation.of("items/potion/antidote_potion.png"));
    }
}
