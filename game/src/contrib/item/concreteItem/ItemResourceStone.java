package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceStone extends Item {
    public ItemResourceStone() {
        super("Stone", "Just a stone.", Animation.of("items/resource/stone.png"));
    }
}
