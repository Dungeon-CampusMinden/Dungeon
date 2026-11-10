package feature.prefabs.types;

import engine.Entity;
import engine.components.PositionComponent;
import engine.network.messages.s2c.ItemState;
import engine.utils.Point;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.components.ItemComponent;
import feature.components.LeverComponent;
import feature.entities.WorldItemBuilder;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Helpers for the saved states of the prefabs in this package, see {@link
 * feature.prefabs.Prefab#captureState}.
 */
final class PrefabStateJson {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PrefabStateJson.class);
  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static final String ITEMS = "items";
  private static final String ON = "on";
  private static final String LOCKED = "locked";

  private PrefabStateJson() {}

  /**
   * Creates an empty state.
   *
   * @return new object node
   */
  static ObjectNode object() {
    return JSON.createObjectNode();
  }

  /**
   * Captures the items of an inventory with their slots.
   *
   * @param inventory inventory to save
   * @return one entry per occupied slot
   */
  static ArrayNode items(InventoryComponent inventory) {
    ArrayNode items = JSON.createArrayNode();
    Item[] slots = inventory.items();
    for (int slot = 0; slot < slots.length; slot++) {
      if (slots[slot] == null) continue;
      try {
        ObjectNode entry = items.addObject();
        entry.put("slot", slot);
        entry.set("item", JSON.valueToTree(ItemState.fromItem(slots[slot])));
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not save the item " + slots[slot].displayName(), exception);
      }
    }
    return items;
  }

  /**
   * Restores items captured by {@link #items(InventoryComponent)}.
   *
   * @param saved saved items
   * @param inventory inventory to fill, its listeners see the items as added
   */
  static void restoreItems(JsonNode saved, InventoryComponent inventory) {
    for (JsonNode entry : saved) {
      try {
        Item item = JSON.treeToValue(entry.get("item"), ItemState.class).toItem();
        if (!inventory.set(entry.path("slot").asInt(-1), item)) inventory.add(item);
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not restore a saved item", exception);
      }
    }
  }

  /**
   * Returns the items captured by {@link #items(InventoryComponent)} in slot order.
   *
   * @param saved saved items
   * @return the restored items
   */
  static List<Item> itemList(JsonNode saved) {
    List<Item> items = new ArrayList<>();
    for (JsonNode entry : saved) {
      try {
        items.add(JSON.treeToValue(entry.get("item"), ItemState.class).toItem());
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not restore a saved item", exception);
      }
    }
    return items;
  }

  /**
   * Captures the items of the first entity with an inventory, e.g. a container.
   *
   * @param entities entities of a spawn
   * @return state with the items, or empty if no entity has an inventory
   */
  static Optional<JsonNode> inventoryState(List<Entity> entities) {
    return entities.stream()
        .flatMap(entity -> entity.fetch(InventoryComponent.class).stream())
        .findFirst()
        .map(
            inventory -> {
              ObjectNode state = object();
              state.set(ITEMS, items(inventory));
              return state;
            });
  }

  /**
   * Returns the saved items of an {@link #inventoryState}.
   *
   * @param saved saved state
   * @return the saved items node
   */
  static JsonNode inventoryItems(JsonNode saved) {
    return saved.path(ITEMS);
  }

  /**
   * Captures the items lying in the level among some entities, with their positions.
   *
   * @param entities entities to look at, entities that are no items are skipped
   * @return one entry per item
   */
  static ArrayNode worldItems(List<Entity> entities) {
    ArrayNode items = JSON.createArrayNode();
    for (Entity entity : entities) {
      Optional<Item> item = entity.fetch(ItemComponent.class).map(ItemComponent::item);
      Optional<PositionComponent> position = entity.fetch(PositionComponent.class);
      if (item.isEmpty() || position.isEmpty()) continue;
      try {
        ObjectNode entry = items.addObject();
        entry.set("item", JSON.valueToTree(ItemState.fromItem(item.get())));
        entry.set("position", point(position.get().position()));
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not save the item " + item.get().displayName(), exception);
      }
    }
    return items;
  }

  /**
   * Creates the items captured by {@link #worldItems} where they were lying.
   *
   * @param saved saved items
   * @return new world item entities, not yet added to the game
   */
  static List<Entity> restoreWorldItems(JsonNode saved) {
    List<Entity> items = new ArrayList<>();
    for (JsonNode entry : saved) {
      try {
        Item item = JSON.treeToValue(entry.get("item"), ItemState.class).toItem();
        items.add(WorldItemBuilder.buildWorldItem(item, point(entry.path("position"))));
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not restore a saved item", exception);
      }
    }
    return items;
  }

  /**
   * Captures the state of a lever or torch.
   *
   * @param entities entities of the spawn
   * @param lockable whether the entity can stop being interactable after a toggle
   * @return the state, or empty if the spawn has no lever
   */
  static Optional<JsonNode> leverState(List<Entity> entities, boolean lockable) {
    return entities.stream()
        .findFirst()
        .flatMap(
            lever ->
                lever
                    .fetch(LeverComponent.class)
                    .map(
                        state ->
                            object()
                                .put(ON, state.isOn())
                                .put(
                                    LOCKED,
                                    lockable && !lever.isPresent(InteractionComponent.class))));
  }

  /**
   * Returns whether a saved lever or torch was on.
   *
   * @param saved saved state, if any
   * @param fallback value without a saved state
   * @return the saved or fallback value
   */
  static boolean leverOn(Optional<JsonNode> saved, boolean fallback) {
    return saved.map(state -> state.path(ON).asBoolean()).orElse(fallback);
  }

  /**
   * Makes a restored lever or torch uninteractable again if it was locked when saved.
   *
   * @param saved saved state, if any
   * @param lever entity of the new spawn
   */
  static void restoreLeverLock(Optional<JsonNode> saved, Entity lever) {
    if (saved.map(state -> state.path(LOCKED).asBoolean()).orElse(false)) {
      lever.remove(InteractionComponent.class);
    }
  }

  /**
   * Captures a position.
   *
   * @param point position to save
   * @return node with x and y
   */
  static ObjectNode point(Point point) {
    ObjectNode node = object();
    node.put("x", point.x());
    node.put("y", point.y());
    return node;
  }

  /**
   * Restores a position captured by {@link #point(Point)}.
   *
   * @param saved saved position
   * @return the position
   */
  static Point point(JsonNode saved) {
    return new Point(saved.path("x").floatValue(), saved.path("y").floatValue());
  }
}
