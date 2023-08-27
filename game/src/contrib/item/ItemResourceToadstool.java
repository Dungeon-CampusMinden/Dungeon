package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceToadstool extends Item {
    public ItemResourceToadstool() {
        super(
                "Toadstool",
                "A toadstool. But don't eat it!",
                Animation.of("items/resource/toadstool.png"));
    }
}
