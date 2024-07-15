package contrib.utils.components.item;

import contrib.item.HealthPotionType;
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
    return switch (RANDOM.nextInt(9)) {
      case 0, 1 -> new ItemPotionHealth(getWeightedRandomHealthPotionType());
      case 2, 3 -> new ItemPotionWater();
      case 4, 5 -> new ItemResourceBerry();
      case 6, 7 -> new ItemResourceEgg();
      default -> new ItemResourceMushroomRed();
    };
  }

  /**
   * This method returns a randomly selected potion type based on a weighted system. The weights are
   * as follows: - 75% WEAK - 20% MEDIUM - 5% GREATER
   *
   * <p>These weights are based on the likelihood of the player finding a potion of a certain type.
   *
   * @return A randomly selected potion type based on the weighted system.
   */
  private static HealthPotionType getWeightedRandomHealthPotionType() {
    HealthPotionType[] types = HealthPotionType.values();

    float[] chances = {0.75f, 0.05f, 0.00f}; /* 75%, 20%, 5% */
    float random = Item.RANDOM.nextFloat();

    for (int i = 0; i < chances.length; i++) {
      if (random < chances[i]) {
        return types[i];
      }
      random -= chances[i];
    }

    return types[types.length - 1];
  }
}
