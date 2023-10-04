package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemGlovesFire extends Item {
    public ItemGlovesFire() {
        super(
                "Fire Gloves",
                "A pair of gloves. But look! It's on fire!",
                Animation.of("items/gloves/fire_gloves.png"));
    }
}
