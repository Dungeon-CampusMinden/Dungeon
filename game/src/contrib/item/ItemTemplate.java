package contrib.item;

import core.utils.components.draw.Animation;

public class ItemTemplate extends Item {
    public ItemTemplate() {
        super("ITEM_NAME", "ITEM_DESCRIPTION", Animation.of("items/book/ITEM_TEXTURE.png"));
    }
}
