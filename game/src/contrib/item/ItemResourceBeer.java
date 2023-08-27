package contrib.item;

import core.utils.components.draw.Animation;

public class ItemResourceBeer extends Item {
    public ItemResourceBeer() {
        super("Beer", "A mug of beer.", Animation.of("items/resource/beer.png"));
    }
}
