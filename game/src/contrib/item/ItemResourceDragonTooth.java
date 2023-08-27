package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceDragonTooth extends Item {
    public ItemResourceDragonTooth() {
        super(
                "Dragon Tooth",
                "A toot of a dragon. Someone really brave must have taken it from the dragon.",
                Animation.of("items/resource/dragon_tooth.png"));
    }
}
