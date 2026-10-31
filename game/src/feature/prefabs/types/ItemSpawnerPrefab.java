package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.components.DecoComponent;
import feature.components.InventoryComponent;
import feature.entities.MiscFactory;
import feature.entities.WorldItemBuilder;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.inventory.Item;
import feature.prefabs.ItemArtifact;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Server-side spawner of a list of items, either lying on the ground, inside a chest, or inside a
 * decoration players can open.
 *
 * <p>Every item is also an artifact, so e.g. an {@link ItemReceptaclePrefab} can wait for one of
 * them. The items are spawned again when the instance is reset.
 */
public final class ItemSpawnerPrefab extends Prefab {

  private static final String TYPE = "item-spawner";
  private static final String DISPLAY_NAME = "Item Spawner";
  private static final String CONTAINER_WORLD = "World";
  private static final String CONTAINER_CHEST = "Chest";
  private static final String CONTAINER_DECO = "Deco";
  private static final int DECO_CAPACITY = 8;
  // Horizontal distance between items lying on the ground.
  private static final float WORLD_ITEM_SPACING = 0.5f;

  /** Position of the items or their container. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Where the items are placed. */
  public static final PrefabProperty<String> CONTAINER =
      PrefabProperty.selection(
          "container",
          "Container",
          CONTAINER_WORLD,
          List.of(CONTAINER_WORLD, CONTAINER_CHEST, CONTAINER_DECO));

  /** Decoration holding the items, if the container is a decoration. */
  public static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
              "deco",
              "Decoration",
              Deco.Cabinet.name(),
              Arrays.stream(Deco.values()).map(Enum::name).toList())
          .visibleWhen(ItemSpawnerPrefab::isDecoContainer);

  /** Title of the decoration in the inventory dialog. */
  public static final PrefabProperty<String> TITLE =
      PrefabProperty.string("title", "Title", "Container", value -> !value.isBlank())
          .visibleWhen(ItemSpawnerPrefab::isDecoContainer);

  private static final List<PrefabProperty<?>> PROPERTIES = allProperties();

  /** Creates the item-spawner definition. */
  public ItemSpawnerPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored item spawner.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public ItemSpawnerPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.copyOf(PrefabItems.OUTPUTS);
  }

  @Override
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    return PrefabItems.isPortAvailable(instance, port);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    List<Item> items = PrefabItems.create(context.level(), instance);
    Point position = value(instance, POSITION);
    return switch (value(instance, CONTAINER)) {
      case CONTAINER_CHEST -> {
        Entity chest = MiscFactory.newChest(Set.of(), position);
        chest.fetch(InventoryComponent.class).ifPresent(inventory -> items.forEach(inventory::add));
        yield List.of(chest);
      }
      case CONTAINER_DECO -> {
        Entity container = context.createEntity(instance.name());
        DecoFactory.createDeco(container, position, Deco.valueOf(value(instance, DECO)));
        container.remove(DecoComponent.class);
        InventoryComponent inventory = new InventoryComponent(DECO_CAPACITY);
        items.forEach(inventory::add);
        container.add(inventory);
        PrefabItems.addInventoryInteraction(container, value(instance, TITLE));
        yield List.of(container);
      }
      default -> {
        List<Entity> entities = new ArrayList<>(items.size());
        float start = -(items.size() - 1) * WORLD_ITEM_SPACING / 2f;
        for (int index = 0; index < items.size(); index++) {
          Point itemPosition = position.translate(start + index * WORLD_ITEM_SPACING, 0f);
          entities.add(WorldItemBuilder.buildWorldItem(items.get(index), itemPosition));
        }
        yield entities;
      }
    };
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
    if (selected) {
      feedback.label(
          position.translate(0f, -0.2f),
          "(" + value(instance, PrefabItems.COUNT) + " in " + value(instance, CONTAINER) + ")");
    }
  }

  /**
   * Returns the artifacts of the spawned items of this instance.
   *
   * @return item kinds by list index, missing entries are not spawned
   */
  public List<ItemArtifact> items() {
    return PrefabItems.OUTPUTS.stream().flatMap(output -> artifact(output).stream()).toList();
  }

  private static boolean isDecoContainer(Map<String, JsonNode> values) {
    JsonNode container = values.get(CONTAINER.key());
    return container != null && CONTAINER_DECO.equals(container.asText());
  }

  private static List<PrefabProperty<?>> allProperties() {
    List<PrefabProperty<?>> properties = new ArrayList<>(List.of(POSITION, CONTAINER, DECO, TITLE));
    properties.addAll(PrefabItems.properties());
    return List.copyOf(properties);
  }
}
