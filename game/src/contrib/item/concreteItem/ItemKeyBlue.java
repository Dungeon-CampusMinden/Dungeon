package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemKeyBlue extends Item {
    public ItemKeyBlue() {
        super("Blue Key", "A blue key", Animation.of("items/key/blue_key.png"));
    }
}
