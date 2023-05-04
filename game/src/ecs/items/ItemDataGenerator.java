package ecs.items;

import graphic.Animation;
import java.util.List;
import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {
    private static final List<String> missingTexture = List.of("animation/missingTexture.png");

    private List<ItemData> templates =
            List.of(
                    new ItemData(
                            ItemType.Basic,
                            new Animation(missingTexture, 1),
                            new Animation(missingTexture, 1),
                            "Buch",
                            "Ein sehr lehrreiches Buch."),
                    new ItemData(
                            ItemType.Basic,
                            new Animation(missingTexture, 1),
                            new Animation(missingTexture, 1),
                            "Tuch",
                            "Ein sauberes Tuch.."),
                    new ItemData(
                            ItemType.Basic,
                            new Animation(missingTexture, 1),
                            new Animation(missingTexture, 1),
                            "Namensschild",
                            "Ein Namensschild wo der Name nicht mehr lesbar ist.."));
    private Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return templates.get(rand.nextInt(templates.size()));
    }
}
