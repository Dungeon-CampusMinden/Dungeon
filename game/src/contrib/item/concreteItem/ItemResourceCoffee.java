package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceCoffee extends Item {
    public ItemResourceCoffee() {
        super(
                "Coffee",
                "A cup of coffee. The morning is only bearable with this.",
                Animation.of("items/resource/coffee.png"));
    }
}
