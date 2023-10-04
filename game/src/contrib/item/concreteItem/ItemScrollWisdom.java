package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemScrollWisdom extends Item {
    public ItemScrollWisdom() {
        super(
                "Wisdom Scroll",
                "A wisdom scroll. Some of it could be very informative.",
                Animation.of("items/book/wisdom_scroll.png"));
    }
}
