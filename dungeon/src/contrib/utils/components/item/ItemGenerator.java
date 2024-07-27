package contrib.utils.components.item;

import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.item.concreteItem.ItemDefault;
import contrib.item.concreteItem.ItemPotionHealth;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
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
   * Creates a default ItemGenerator with all items added with a weight of 1.
   *
   * <p>This method is a factory method that creates an instance of ItemGenerator, adds all items to
   * it with a weight of 1, and then returns the instance. This is useful when you want a simple
   * ItemGenerator that treats all items equally in terms of their probability of being generated.
   *
   * <p>For {@link ItemPotionHealth}, the default health potion is replaced with a random health
   * potion using {@link HealthPotionType#randomType()}.
   *
   * @return An ItemGenerator with all {@link Item#registeredItems()} (except {@link ItemDefault})
   *     added with a weight of 1.
   */
  public static ItemGenerator defaultItemGenerator() {
    ItemGenerator ig = new ItemGenerator();
    ig.addAllItems();
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

  private void addAllItems() {
    List<Class<? extends Item>> itemClasses = new ArrayList<>(Item.registeredItems().values());
    itemClasses.remove(ItemDefault.class); // Remove the default item

    // Replace the default health potion with a random health potion
    itemClasses.remove(ItemPotionHealth.class);
    addItem(() -> new ItemPotionHealth(HealthPotionType.randomType()), 1.0);

    for (Class<? extends Item> itemClass : itemClasses) {
      addItem(createItemSupplier(itemClass), 1.0);
    }
  }

  /**
   * Creates a supplier that creates an instance of an item class using reflection.
   *
   * @param itemClass The class of the item to create
   * @return An instance of the item class, or null if an instance could not be created
   */
  private Supplier<Item> createItemSupplier(Class<? extends Item> itemClass) {
    return () -> {
      try {
        return itemClass.getDeclaredConstructor().newInstance();
      } catch (InstantiationException
          | IllegalAccessException
          | NoSuchMethodException
          | InvocationTargetException e) {
        throw new IllegalStateException(
            "Could not create an instance of " + itemClass.getSimpleName(), e);
      }
    };
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
