package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceMushroomBrown extends Item {
    public ItemResourceMushroomBrown() {
        super(
                "Brown Mushroom",
                "A brown mushroom.",
                Animation.of("items/resource/mushroom_brown.png"));
    }
}
