package contrib.item;

import core.utils.components.draw.Animation;

public class ItemScrollMagic extends Item {
    public ItemScrollMagic() {
        super(
                "Magic Scroll",
                "A magic scroll. It's said that it was forged by the gods.",
                Animation.of("items/book/magic_scroll.png"));
    }
}
