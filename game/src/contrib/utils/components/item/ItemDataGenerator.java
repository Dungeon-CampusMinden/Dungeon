package contrib.utils.components.item;

import contrib.item.concreteItem.ItemDefault;

import core.utils.components.draw.Animation;

import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {

    private final Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public contrib.item.Item generateItemData() {
        return new ItemDefault(
                "Default Item",
                "Default Item description",
                new Animation("animation/missing_texture.png"),
                new Animation("animation/missing_texture.png"));
    }
}
