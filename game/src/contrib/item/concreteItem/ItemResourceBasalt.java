package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceBasalt extends Item {
    public ItemResourceBasalt() {
        super("Basalt", "Its just basalt.", Animation.of("items/resource/basalt.png"));
    }
}
