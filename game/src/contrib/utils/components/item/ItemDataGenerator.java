package contrib.utils.components.item;

import core.utils.components.draw.Animation;

import java.util.List;
import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {
    private static final List<String> missingTexture = List.of("animation/missingTexture.png");

    private final List<ItemData> templates =
            List.of(
                    new ItemData(
                            ItemType.Basic,
                            new Animation(List.of("animation/bronzeBow.png"), 1),
                            new Animation(List.of("animation/bronzeBow.png"), 1),
                            "Buch",
                            "Ein sehr lehrreiches Buch."),
                    new ItemData(
                            ItemType.Basic,
                            new Animation(List.of("animation/bag_small.png"), 1),
                            new Animation(List.of("animation/bag_small.png"), 1),
                            "Tuch",
                            "Ein sauberes Tuch.."),
                    new ItemData(
                            ItemType.Active,
                            new Animation(List.of("animation/blueBook.png"), 1),
                            new Animation(List.of("animation/blueBook.png"), 1),
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
