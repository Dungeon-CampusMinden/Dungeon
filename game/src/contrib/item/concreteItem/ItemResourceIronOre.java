package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceIronOre extends Item {
    public ItemResourceIronOre() {
        super("Iron Ore", "A piece of iron ore.", Animation.of("items/resource/iron_ore.png"));
    }
}
