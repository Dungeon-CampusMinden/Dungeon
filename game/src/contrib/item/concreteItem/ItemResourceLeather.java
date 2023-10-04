package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceLeather extends Item {
    public ItemResourceLeather() {
        super(
                "Leather",
                "A piece of leather, probably torn of a cow.",
                Animation.of("items/resource/leather.png"));
    }
}
