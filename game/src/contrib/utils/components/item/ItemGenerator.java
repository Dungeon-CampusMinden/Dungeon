package contrib.utils.components.item;

import contrib.item.concreteItem.*;

import java.util.Random;

/** Generator which creates a random Item based on the Templates prepared. */
public class ItemGenerator {

    private final Random rand = new Random();

    /**
     * Generates a new random Item.
     *
     * @return a new random Item.
     */
    public contrib.item.Item generateItemData() {
        return switch (rand.nextInt(8)) {
            case 0 -> new ItemPotionHealth();
            case 1, 2 -> new ItemPotionWater();
            case 3, 4 -> new ItemResourceBerry();
            case 5, 6 -> new ItemResourceEgg();
            default -> new ItemResourceMushroomRed();
        };
    }
}
