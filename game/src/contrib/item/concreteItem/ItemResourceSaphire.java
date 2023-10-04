package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceSaphire extends Item {
    public ItemResourceSaphire() {
        super(
                "Saphire",
                "A saphire. It's said that it has magical powers.",
                Animation.of("items/resource/saphire.png"));
    }
}
