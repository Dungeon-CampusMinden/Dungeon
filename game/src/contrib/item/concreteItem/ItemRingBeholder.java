package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemRingBeholder extends Item {
    public ItemRingBeholder() {
        super(
                "Beholder Ring",
                "A ring that is used by beholders.",
                Animation.of("items/ring/beholder_ring.png"));
    }
}
