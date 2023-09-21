package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourcePretzel extends Item {
    public ItemResourcePretzel() {
        super(
                "Pretzel",
                "A pretzel is a typically german type of baked pastry made from dough that is shaped into a knot.",
                Animation.of("items/resource/pretzel.png"));
    }
}
