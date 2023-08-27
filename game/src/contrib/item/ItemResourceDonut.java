package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceDonut extends Item {
    public ItemResourceDonut() {
        super(
                "Donut",
                "A donut. Don't let the cops see you with this.",
                Animation.of("items/resource/donut.png"));
    }
}
