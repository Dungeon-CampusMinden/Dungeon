package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceEmerald extends Item {
    public ItemResourceEmerald() {
        super(
                "Emerald",
                "An emerald. It's said that it has magical powers.",
                Animation.of("items/resource/emerald.png"));
    }
}
