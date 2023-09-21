package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemGlovesSteel extends Item {
    public ItemGlovesSteel() {
        super(
                "Steel Gloves",
                "A pair of gloves. Made of steel.",
                Animation.of("items/gloves/steel_gloves.png"));
    }
}
