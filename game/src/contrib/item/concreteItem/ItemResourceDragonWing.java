package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceDragonWing extends Item {
    public ItemResourceDragonWing() {
        super(
                "Dragon Wing",
                "A wing of a dragon. Someone really brave must have taken it from the dragon.",
                Animation.of("items/resource/dragon_wing.png"));
    }
}
