package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceCheese extends Item {
    public ItemResourceCheese() {
        super("Cheese", "A piece of cheese.", Animation.of("items/resource/cheese.png"));
    }
}
