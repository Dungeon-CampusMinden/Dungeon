package contrib.utils.components.item;

import contrib.item.concreteItem.*;

import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {

    private final Random rand = new Random();

    /**
     * @return a new randomItemData
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
