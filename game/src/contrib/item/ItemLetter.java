package contrib.item;

import core.utils.components.draw.Animation;

public class ItemLetter extends Item {
    public ItemLetter() {
        super("Letter", "A letter.", Animation.of("items/book/letter.png"));
    }
}
