package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceSulphur extends Item {
    public ItemResourceSulphur() {
        super("Sulphur", "Some sulphur.", Animation.of("items/resource/sulphur.png"));
    }
}
