package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemKeyRed extends Item {
    public ItemKeyRed() {
        super("Red Key", "A red key.", Animation.of("items/key/red_key.png"));
    }
}
