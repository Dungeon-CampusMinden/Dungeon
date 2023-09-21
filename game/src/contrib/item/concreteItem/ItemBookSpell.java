package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemBookSpell extends Item {
    public ItemBookSpell() {
        super(
                "Spell Book",
                "A spell book. It contains many interesting sentences.",
                Animation.of("items/book/spell_book.png"));
    }
}
