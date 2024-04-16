package contrib.utils.components.item;

import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.item.concreteItem.*;
import java.util.List;

/**
 * Generator which creates a random Item based on the Templates provided.
 *
 * <p>USe {@link #generateItemData()} to get a random {@link Item}.
 */
public final class ItemGenerator {

  /**
   * Generates a new random Item. Randomly selects a registered Item from the Item.ITEMS map.
   *
   * @return A new random Item.
   */
  public static Item generateItemData() {
    // All registered items except the default item
    List<Class<? extends Item>> items = new java.util.ArrayList<>(Item.ITEMS.values());
    items.remove(ItemDefault.class); // Remove the default item

    int randomIndex = Item.RANDOM.nextInt(items.size());
    Class<? extends Item> item = items.get(randomIndex);

    if (item.getSimpleName().contains("Potion")) { // Prevent other potions from being generated
      if (Item.RANDOM.nextBoolean()) {
        if ((Item.RANDOM.nextBoolean())) return new ItemResourceMushroomRed();
        else return new ItemResourceBerry();
      }
      return new ItemPotionHealth(getWeightedRandomHealthPotionType());
    }

    if (item.getSimpleName().contains("Egg")) { // Prevent eggs from being generated
      return new ItemPotionWater();
    }

    try {
      return item.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to create new instance of " + item.getSimpleName(), e);
    }
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
