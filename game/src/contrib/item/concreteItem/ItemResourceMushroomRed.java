package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceMushroomRed extends Item {
    public ItemResourceMushroomRed() {
        super("Red Mushroom", "A red mushroom.", Animation.of("items/resource/mushroom_red.png"));
    }
}
