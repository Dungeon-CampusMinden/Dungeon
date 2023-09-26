package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceWood extends Item {
    public ItemResourceWood() {
        super("Wood", "A piece of wood.", Animation.of("items/resource/wood.png"));
    }
}
