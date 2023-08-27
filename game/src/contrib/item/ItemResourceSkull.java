package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceSkull extends Item {
    public ItemResourceSkull() {
        super(
                "Skull",
                "A skull. I wonder who it used to be?",
                Animation.of("items/resource/skull.png"));
    }
}
