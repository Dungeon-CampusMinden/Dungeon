package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceFlowerRed extends Item {
    public ItemResourceFlowerRed() {
        super("Red Flower", "A red flower.", Animation.of("items/resource/flower_red.png"));
    }
}
