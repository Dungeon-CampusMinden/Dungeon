package contrib.utils.components.item;

import core.utils.components.draw.Animation;

import java.util.List;
import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {
    private static final List<String> boneTexture = List.of("items/bone.png");
    private static final List<String> scrollTexture = List.of("items/scroll.png");
    private static final List<String> leafTexture = List.of("items/leaf.png");

    private final List<ItemData> templates =
            List.of(
                    new ItemData(
                            ItemType.Basic,
                            new Animation(scrollTexture, 1),
                            new Animation(scrollTexture, 1),
                            "Schriftrolle",
                            "Eine sehr lehrreiche Schriftrolle."),
                    new ItemData(
                            ItemType.Basic,
                            new Animation(boneTexture, 1),
                            new Animation(boneTexture, 1),
                            "Knochen",
                            "Ein harter Knochen.."),
                    new ItemData(
                            ItemType.Active,
                            new Animation(leafTexture, 1),
                            new Animation(leafTexture, 1),
                            "Blatt",
                            "Ein Blatt..."));
    private final Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return templates.get(rand.nextInt(templates.size()));
    }
}
