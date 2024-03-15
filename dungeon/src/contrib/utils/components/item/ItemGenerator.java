package contrib.utils.components.item;

import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.item.concreteItem.*;
import java.util.List;
import java.util.Random;

/**
 * Generator which creates a random Item based on the Templates provided.
 *
 * <p>USe {@link #generateItemData()} to get a random {@link Item}.
 */
public final class ItemGenerator {

  private static final Random RANDOM = new Random();

  /**
   * Generates a new random Item. Randomly selects a registered Item from the Item.ITEMS map.
   *
   * @return A new random Item.
   */
  public static Item generateItemData() {
    // Item.ITEMS
    List<Class<? extends Item>> items = new java.util.ArrayList<>(Item.ITEMS.values());
    items.remove(ItemDefault.class); // Remove the default item

    int randomIndex = RANDOM.nextInt(items.size());
    Class<? extends Item> item = items.get(randomIndex);

    if (item.equals(ItemPotionHealth.class)) {
      return new ItemPotionHealth(getWeightedRandomPotionType());
    }

    try {
      return item.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to create new instance of " + item.getSimpleName(), e);
    }
  }

  /**
   * This method is used to generate a random potion type based on a weighted system. The types at
   * the beginning of the array have a higher chance of being selected.
   *
   * @return A randomly selected potion type based on the weighted system.
   */
  private static HealthPotionType getWeightedRandomPotionType() {
    HealthPotionType[] types = HealthPotionType.values();

    int[] weights = new int[types.length];

    // Calculate the cumulative weights
    int totalWeight = 0;
    for (int i = 0; i < types.length; i++) {
      weights[i] = totalWeight += (types.length - i);
    }

    int randomWeight = RANDOM.nextInt(totalWeight);

    // Find the index of the first weight that is greater than the random number
    for (int i = 0; i < weights.length; i++) {
      if (randomWeight < weights[i]) {
        return types[i];
      }
    }

    // If no match is found, fallback to the weakest potion
    return types[0];
  }
}
