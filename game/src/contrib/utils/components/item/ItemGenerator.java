package contrib.utils.components.item;

import contrib.item.Item;
import contrib.item.concreteItem.*;

import java.util.Random;

/**
 * Generator which creates a random Item based on the Templates provided.
 *
 * <p>USe {@link #generateItemData()} to get a random {@link Item}.
 */
public final class ItemGenerator {

    private static final Random RANDOM = new Random();

    /**
     * Generates a new random Item.
     *
     * @return A new random Item.
     */
    public static Item generateItemData() {
        return switch (RANDOM.nextInt(8)) {
            case 0 -> new ItemPotionHealth();
            case 1, 2 -> new ItemPotionWater();
            case 3, 4 -> new ItemResourceBerry();
            case 5, 6 -> new ItemResourceEgg();
            default -> new ItemResourceMushroomRed();
        };
    }
}
