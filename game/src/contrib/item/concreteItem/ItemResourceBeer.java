package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceBeer extends Item {
    public ItemResourceBeer() {
        super("Beer", "A mug of beer.", Animation.of("items/resource/beer.png"));
    }
}
