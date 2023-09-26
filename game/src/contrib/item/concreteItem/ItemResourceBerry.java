package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceBerry extends Item {
    public ItemResourceBerry() {
        super("Berry", "A berry.", Animation.of("items/resource/berry.png"));
    }
}
