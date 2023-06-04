package contrib.utils.components.item;

import core.utils.components.draw.Animation;

import java.util.List;
import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {
    private static final List<String> bowTexture = List.of("animation/bronzeBow.png");
    private static final List<String> bookTexture = List.of("animation/blueBook.png");
    private static final List<String> bagTexture = List.of("animation/bag_small.png");

    private final List<ItemData> templates =
            List.of(
                    new ItemData(
                            ItemType.Basic,
                            new Animation(bowTexture, 1),
                            new Animation(bowTexture, 1),
                            "Buch",
                            "Ein sehr lehrreiches Buch."),
                    new ItemData(
                            ItemType.Basic,
                            new Animation(bagTexture, 1),
                            new Animation(bagTexture, 1),
                            "Tuch",
                            "Ein sauberes Tuch.."),
                    new ItemData(
                            ItemType.Active,
                            new Animation(bookTexture, 1),
                            new Animation(bookTexture, 1),
                            "Namensschild",
                            "Ein Namensschild wo der Name nicht mehr lesbar ist.."));
    private final Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return templates.get(rand.nextInt(templates.size()));
    }
}
