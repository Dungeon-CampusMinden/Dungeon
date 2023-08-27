package contrib.item;

import core.utils.components.draw.Animation;

public class ItemNecklaceGolden extends Item {
    public ItemNecklaceGolden() {
        super(
                "Golden Necklace",
                "A golden necklace.",
                Animation.of("items/necklace/golden_necklace.png"));
    }
}
