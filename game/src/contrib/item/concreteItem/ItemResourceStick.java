package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceStick extends Item {
    public ItemResourceStick() {
        super("Stick", "A stick.", Animation.of("items/resource/stick.png"));
    }
}
