package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceGold extends Item {
    public ItemResourceGold() {
        super("Gold", "A piece of gold.", Animation.of("items/resource/gold.png"));
    }
}
