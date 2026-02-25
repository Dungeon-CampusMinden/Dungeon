package contrib.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry for item IDs, classes, and optional item factories.
 *
 * <p>IDs should remain stable because they are used by crafting recipes and network payloads.
 */
public final class ItemRegistry {
  private static final Map<String, Class<? extends Item>> ITEMS = new HashMap<>();
  private static final Map<String, ItemFactory> FACTORIES = new HashMap<>();

  private ItemRegistry() {}

  /**
   * Registers an item class under a stable ID.
   *
   * @param id the registry ID
   * @param clazz the item class
   * @throws IllegalArgumentException if the ID is null or blank
   * @throws IllegalStateException if the ID is already registered with a different class
   */
  public static void register(String id, Class<? extends Item> clazz) {
    String normalized = normalizeId(id);
    Objects.requireNonNull(clazz, "clazz");
    Class<? extends Item> existing = ITEMS.get(normalized);
    if (existing != null && !existing.equals(clazz)) {
      throw new IllegalStateException(
          "Item ID '" + normalized + "' already registered for " + existing.getName());
    }
    ITEMS.put(normalized, clazz);
  }

  /**
   * Registers an item class and its factory under a stable ID.
   *
   * @param id the registry ID
   * @param clazz the item class
   * @param factory the item factory
   * @throws IllegalArgumentException if the ID is null or blank
   * @throws IllegalStateException if the ID is already registered with a different class or factory
   */
  public static void register(String id, Class<? extends Item> clazz, ItemFactory factory) {
    register(id, clazz);
    registerFactory(id, factory);
  }

  /**
   * Registers an item class using its simple class name as the ID.
   *
   * @param clazz the item class
   */
  public static void register(Class<? extends Item> clazz) {
    Objects.requireNonNull(clazz, "clazz");
    register(clazz.getSimpleName(), clazz);
  }

  /**
   * Registers an item class and factory using the simple class name as the ID.
   *
   * @param clazz the item class
   * @param factory the item factory
   */
  public static void register(Class<? extends Item> clazz, ItemFactory factory) {
    Objects.requireNonNull(clazz, "clazz");
    register(clazz.getSimpleName(), clazz, factory);
  }

  /**
   * Registers a factory for an existing item ID.
   *
   * @param id the registry ID
   * @param factory the item factory
   * @throws IllegalArgumentException if the ID is null or blank
   * @throws IllegalStateException if a different factory is already registered for the ID
   */
  public static void registerFactory(String id, ItemFactory factory) {
    String normalized = normalizeId(id);
    Objects.requireNonNull(factory, "factory");
    ItemFactory existing = FACTORIES.get(normalized);
    if (existing != null && !existing.equals(factory)) {
      throw new IllegalStateException(
          "Item factory already registered for ID '" + normalized + "'");
    }
    FACTORIES.put(normalized, factory);
  }

  /**
   * Looks up the item class for a given ID.
   *
   * @param id the registry ID
   * @return the item class if registered
   * @throws IllegalArgumentException if the ID is null or blank
   */
  public static Optional<Class<? extends Item>> lookup(String id) {
    String normalized = normalizeId(id);
    ensureRegistryInitialized();
    return Optional.ofNullable(ITEMS.get(normalized));
  }

  /**
   * Returns an immutable snapshot of registered item entries.
   *
   * @return the current registry entries
   */
  public static Map<String, Class<? extends Item>> entries() {
    ensureRegistryInitialized();
    return Map.copyOf(ITEMS);
  }

  /**
   * Resolves the registry ID for a given item class.
   *
   * @param clazz the item class
   * @return the registered ID if found
   * @throws IllegalArgumentException if the class is null
   * @throws IllegalStateException if multiple IDs are registered for the class
   */
  public static Optional<String> idFor(Class<? extends Item> clazz) {
    Objects.requireNonNull(clazz, "clazz");
    ensureRegistryInitialized();
    String found = null;
    for (Map.Entry<String, Class<? extends Item>> entry : ITEMS.entrySet()) {
      if (!entry.getValue().equals(clazz)) {
        continue;
      }
      if (found != null) {
        throw new IllegalStateException(
            "Multiple IDs registered for item class: " + clazz.getName());
      }
      found = entry.getKey();
    }
    return Optional.ofNullable(found);
  }

  /**
   * Resolves the registry ID for a given item instance.
   *
   * @param item the item instance
   * @return the registered ID
   * @throws IllegalArgumentException if the item is null or unregistered
   * @throws IllegalStateException if multiple IDs are registered for the class
   */
  public static String idFor(Item item) {
    Objects.requireNonNull(item, "item");
    return idFor(item.getClass())
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Item class not registered: " + item.getClass().getName()));
  }

  /**
   * Creates an item via the registered factory.
   *
   * @param id the registry ID
   * @param data the item data payload
   * @return the created item if a factory is registered
   * @throws IllegalArgumentException if the ID is null or blank
   * @throws IllegalStateException if the factory returns null
   */
  public static Optional<Item> create(String id, Map<String, String> data) {
    String normalized = normalizeId(id);
    ensureRegistryInitialized();
    ItemFactory factory = FACTORIES.get(normalized);
    if (factory == null) {
      return Optional.empty();
    }
    Map<String, String> safeData = data == null ? Map.of() : Map.copyOf(data);
    Item item = factory.create(safeData);
    if (item == null) {
      throw new IllegalStateException("Item factory for ID '" + normalized + "' returned null.");
    }
    return Optional.of(item);
  }

  /** Factory for creating items from serialized item data. */
  @FunctionalInterface
  public interface ItemFactory {
    /**
     * Creates an item from the provided data.
     *
     * @param data item data payload
     * @return the created item
     */
    Item create(Map<String, String> data);
  }

  static boolean isRegistered(Class<? extends Item> clazz) {
    return ITEMS.containsValue(clazz);
  }

  private static void ensureRegistryInitialized() {
    if (ITEMS.isEmpty()) {
      Item.ensureRegistryInitialized();
    }
  }

  private static String normalizeId(String id) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Item ID must not be null or blank.");
    }
    return id;
  }
}
