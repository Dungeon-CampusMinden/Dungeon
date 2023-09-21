package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceSteel extends Item {
    public ItemResourceSteel() {
        super("Steel", "A piece of steel.", Animation.of("items/resource/steel.png"));
    }
}
