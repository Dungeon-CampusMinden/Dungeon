package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceEgg extends Item {
    public ItemResourceEgg() {
        super(
                "Egg",
                "An egg. What was there before? The chicken or the egg?",
                Animation.of("items/resource/egg.png"));
    }
}
