package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import feature.prefabs.ArtifactOutput;
import feature.prefabs.ArtifactType;
import feature.prefabs.ItemArtifact;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRuntime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * Shared list of items authored on a prefab, e.g. the contents of an item spawner.
 *
 * <p>The list has up to {@link #MAX_ITEMS} entries, each chosen from the {@link ItemRegistry}.
 * Every entry is also exposed as an item artifact, so other prefabs can e.g. wait for one of the
 * items.
 */
final class PrefabItems {

  /** Maximum number of items in the list. */
  static final int MAX_ITEMS = 6;

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PrefabItems.class);
  private static final String DEFAULT_ITEM = "ItemKey";

  /** Number of items in the list. */
  static final PrefabProperty<Integer> COUNT =
      PrefabProperty.integer("item-count", "Item Count", 1, 1, MAX_ITEMS);

  /** Registry ID of every list entry, only shown up to {@link #COUNT}. */
  static final List<PrefabProperty<String>> ITEMS = itemProperties();

  /** Artifact of every list entry, published while the instance is spawned. */
  static final List<ArtifactOutput<ItemArtifact>> OUTPUTS = itemOutputs();

  private PrefabItems() {}

  /**
   * Returns the properties of the item list.
   *
   * @return the count followed by the entries
   */
  static List<PrefabProperty<?>> properties() {
    List<PrefabProperty<?>> properties = new ArrayList<>();
    properties.add(COUNT);
    properties.addAll(ITEMS);
    return properties;
  }

  /**
   * Creates the items of an instance and publishes their artifacts.
   *
   * <p>Entries whose item can not be created, e.g. because its class is not registered in this
   * session, are skipped with a warning.
   *
   * @param level owning level
   * @param instance normalized instance
   * @return the created items, in list order
   */
  static List<Item> create(ILevel level, PrefabInstance instance) {
    List<Item> items = new ArrayList<>();
    int count = COUNT.get(instance);
    for (int index = 0; index < count; index++) {
      String id = ITEMS.get(index).get(instance);
      Optional<Item> item = create(id);
      if (item.isEmpty()) {
        LOGGER.warn("Prefab '" + instance.name() + "' can not create item '" + id + "'.");
        continue;
      }
      items.add(item.get());
      PrefabRuntime.publish(
          level, instance.name(), OUTPUTS.get(index), ItemArtifact.of(item.get()));
    }
    return items;
  }

  /**
   * Adds an interaction that opens the inventory of a container next to the player's inventory.
   *
   * @param container container entity with an {@link InventoryComponent}
   * @param title title of the container in the dialog
   */
  static void addInventoryInteraction(Entity container, String title) {
    container.add(
        new InteractionComponent(
            new Interaction(
                (interacted, interactor) -> {
                  if (interactor.fetch(InventoryComponent.class).isEmpty()) return;
                  DialogContext dialog =
                      DialogContext.builder()
                          .type(DialogType.DefaultTypes.DUAL_INVENTORY)
                          .put(DialogContextKeys.ENTITY, interactor.id())
                          .put(DialogContextKeys.SECONDARY_ENTITY, interacted.id())
                          .put(DialogContextKeys.SECONDARY_TITLE, title)
                          .put(DialogContextKeys.OWNER_ENTITY, interactor.id())
                          .build();
                  interactor.add(new UIComponent(dialog, true, interactor.id()));
                })));
  }

  private static Optional<Item> create(String id) {
    try {
      return new ItemArtifact(id, Map.of(), id).create();
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
  }

  /**
   * Lists the registered items that can be created without further data.
   *
   * @return registry IDs, sorted
   */
  private static List<String> choices() {
    return ItemRegistry.entries().entrySet().stream()
        .filter(entry -> ItemArtifact.isInstantiable(entry.getValue()))
        .map(Map.Entry::getKey)
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();
  }

  private static List<PrefabProperty<String>> itemProperties() {
    List<PrefabProperty<String>> properties = new ArrayList<>(MAX_ITEMS);
    for (int index = 0; index < MAX_ITEMS; index++) {
      int number = index + 1;
      PrefabProperty<String> property =
          PrefabProperty.openSelection(
              "item-" + number, "Item " + number, DEFAULT_ITEM, PrefabItems::choices);
      property.visibleWhen(values -> count(values) >= number);
      properties.add(property);
    }
    return List.copyOf(properties);
  }

  private static List<ArtifactOutput<ItemArtifact>> itemOutputs() {
    List<ArtifactOutput<ItemArtifact>> outputs = new ArrayList<>(MAX_ITEMS);
    for (int index = 0; index < MAX_ITEMS; index++) {
      int number = index + 1;
      outputs.add(ArtifactOutput.of("item-" + number, "Item " + number, ArtifactType.ITEM));
    }
    return List.copyOf(outputs);
  }

  private static int count(Map<String, JsonNode> values) {
    JsonNode count = values.get(COUNT.key());
    return count == null || !count.isInt() ? COUNT.defaultValue() : count.asInt();
  }
}
