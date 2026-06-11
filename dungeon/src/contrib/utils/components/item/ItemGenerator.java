package contrib.utils.components.item;

import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.item.concreteItem.ItemBigKey;
import contrib.item.concreteItem.ItemFairy;
import contrib.item.concreteItem.ItemHammer;
import contrib.item.concreteItem.ItemHeart;
import contrib.item.concreteItem.ItemKey;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.item.concreteItem.ItemResourceEgg;
import contrib.item.concreteItem.ItemResourceMushroomRed;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.item.concreteItem.ItemWoodenBow;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/**
 * A flexible random item generator that allows for custom weighting of items.
 *
 * <p>Items can be added to the generator with a specified weight, which will affect the probability
 * of that item being selected when generating a random item. The weights do not need to sum to 1.
 *
 * @see Item
 * @see contrib.entities.MiscFactory#generateRandomItems(int, int) generateRandomItems
 */
public class ItemGenerator {
  private final Random random;
  private final Map<Supplier<Item>, Double> weightedItems;
  private double totalWeight;

  /** Constructs an ItemRandomGenerator with a random seed. */
  public ItemGenerator() {
    this.random = new Random();
    this.weightedItems = new HashMap<>();
    this.totalWeight = 0.0;
  }

  /**
   * Creates a default ItemGenerator with all default loot items added with a weight of 1.
   *
   * <p>This method is a factory method that creates an instance of ItemGenerator, adds all generic
   * loot items to it with a weight of 1, and then returns the instance. This is useful when you
   * want a simple ItemGenerator that treats all default loot equally in terms of its probability of
   * being generated.
   *
   * <p>For {@link ItemPotionHealth}, the default health potion is replaced with a random health
   * potion using {@link HealthPotionType#randomType()}.
   *
   * @return An ItemGenerator with default loot items added with a weight of 1.
   */
  public static ItemGenerator defaultItemGenerator() {
    ItemGenerator ig = new ItemGenerator();
    ig.addDefaultLootItems();
    return ig;
  }

  /**
   * Adds an item to the generator with a specified weight.
   *
   * @param itemSupplier A supplier that creates an instance of the item
   * @param weight The weight of the item (higher weight means higher probability)
   */
  public void addItem(Supplier<Item> itemSupplier, double weight) {
    weightedItems.put(itemSupplier, weight);
    totalWeight += weight;
  }

  private void addDefaultLootItems() {
    addItem(() -> new ItemPotionHealth(HealthPotionType.randomType()), 1.0);
    addItem(ItemPotionWater::new, 1.0);
    addItem(ItemResourceBerry::new, 1.0);
    addItem(ItemResourceEgg::new, 1.0);
    addItem(ItemResourceMushroomRed::new, 1.0);
    addItem(ItemWoodenArrow::new, 1.0);
    addItem(ItemWoodenBow::new, 1.0);
    addItem(ItemBigKey::new, 1.0);
    addItem(ItemFairy::new, 1.0);
    addItem(ItemHammer::new, 1.0);
    addItem(ItemHeart::new, 1.0);
    addItem(ItemKey::new, 1.0);
  }

  /**
   * Generates a random item based on the current weights in the generator.
   *
   * @return A randomly selected item
   * @throws IllegalStateException if no items have been added to the generator
   */
  public Item generateItemData() {
    if (weightedItems.isEmpty()) {
      throw new IllegalStateException("No items added to the generator");
    }

    double randomValue = random.nextDouble() * totalWeight;
    for (Map.Entry<Supplier<Item>, Double> entry : weightedItems.entrySet()) {
      randomValue -= entry.getValue();
      if (randomValue <= 0) {
        return entry.getKey().get();
      }
    }

    // Fallback, should never be reached
    return weightedItems.keySet().iterator().next().get();
  }

  /** Resets the generator, removing all added items and weights. */
  public void reset() {
    weightedItems.clear();
    totalWeight = 0.0;
  }
}
