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

    if (item.getSimpleName().contains("Potion")) { // Prevent other potions from being generated
      return new ItemPotionHealth(getWeightedRandomHealthPotionType());
    }

    try {
      return item.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to create new instance of " + item.getSimpleName(), e);
    }
  }

  /**
   * This method returns a randomly selected potion type based on a weighted system. The weights are
   * as follows: - 70% WEAK - 25% MEDIUM - 5% GREATER
   *
   * <p>These weights are based on the likelihood of the player finding a potion of a certain type.
   *
   * @return A randomly selected potion type based on the weighted system.
   */
  private static HealthPotionType getWeightedRandomHealthPotionType() {
    HealthPotionType[] types = HealthPotionType.values();

    float[] chances = {0.7f, 0.25f, 0.05f}; /* 70%, 25%, 5% */
    float random = RANDOM.nextFloat();

    for (int i = 0; i < chances.length; i++) {
      if (random < chances[i]) {
        return types[i];
      }
      random -= chances[i];
    }

    return types[types.length - 1];
  }
}
