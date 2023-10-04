package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemNecklaceMagic extends Item {
    public ItemNecklaceMagic() {
        super(
                "Magic Necklace",
                "A magic necklace. It's said that it was forged by the gods.",
                Animation.of("items/necklace/magic_necklace.png"));
    }
}
