package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceLeaf extends Item {
    public ItemResourceLeaf() {
        super("Leaf", "A leaf.", Animation.of("items/resource/leaf.png"));
    }
}
