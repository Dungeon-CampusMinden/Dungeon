package feature.prefabs.types;

import feature.prefabs.Prefab;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Splits the combined door prefabs of level format version 4 and older into a standalone prefab and
 * a linked {@link DoorControlPrefab}.
 *
 * <table>
 *   <caption>Migrated types</caption>
 *   <tr><th>Old type</th><th>New type</th><th>Door Control links</th></tr>
 *   <tr><td>door-keypad</td><td>keypad</td><td>Open ← Correct Code</td></tr>
 *   <tr><td>door-text-keypad</td><td>text-keypad</td><td>Open ← Correct Answer</td></tr>
 *   <tr><td>door-lever</td><td>lever</td><td>Open ← Switched On, Close ← Switched Off
 *       (swapped if the door was open at start)</td></tr>
 *   <tr><td>pressure-plate</td><td>pressure-plate</td><td>Open ← Pressed, Close ← Released</td>
 *   </tr>
 * </table>
 *
 * <p>The standalone prefab keeps the name of the old instance, so links to its events and level
 * handlers looking it up by name keep working. Old instances targeting the same door with the same
 * initial door state share one Door Control named {@code <group>/door-<x>-<y>}. The Door Control is
 * active if any of its sources is. A Door Control with a single source also takes over the
 * lifecycle links (Activate, Deactivate, Reset) of that source, so e.g. activating a formerly
 * inactive Door + Keypad still closes its door only once it is activated.
 *
 * <p>The migration works on the JSON records, so the old types do not have to be registered.
 */
public final class CombinedDoorPrefabMigration {

  private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
  private static final List<String> LIFECYCLE_PORTS =
      List.of(Prefab.ACTIVATE.key(), Prefab.DEACTIVATE.key(), Prefab.RESET.key());
  private static final String OPEN = DoorControlPrefab.OPEN.key();
  private static final String CLOSE = DoorControlPrefab.CLOSE.key();

  private CombinedDoorPrefabMigration() {}

  /**
   * Returns the prefab records with all combined door prefabs split. Records of other types and
   * malformed records are returned unchanged.
   *
   * @param records prefab records of a level file
   * @param log receives a description of each migrated record
   * @return migrated records
   */
  public static ArrayNode migrate(ArrayNode records, Consumer<String> log) {
    Set<String> names = new HashSet<>();
    for (JsonNode record : records) {
      JsonNode name = record.get("name");
      if (name != null && name.isTextual()) names.add(name.asText());
    }

    ArrayNode result = NODES.arrayNode();
    Map<DoorKey, DoorGroup> groups = new LinkedHashMap<>();
    for (JsonNode node : records) {
      Split split = split(node);
      if (split == null) {
        result.add(node);
        continue;
      }
      ObjectNode record = (ObjectNode) node;
      String name = record.get("name").asText();
      String oldType = record.get("type").asText();
      record.put("type", split.newType());
      result.add(record);

      DoorKey key = new DoorKey(split.door().x(), split.door().y(), split.closedAtStart());
      DoorGroup group = groups.get(key);
      if (group == null) {
        group = new DoorGroup(doorControl(controlName(name, key, names), split));
        groups.put(key, group);
        result.add(group.control());
      }
      group.sources().add(record);
      for (Map.Entry<String, String> link : split.links().entrySet()) {
        addLink(group.control(), link.getKey(), name, link.getValue());
      }
      log.accept(
          "Split '"
              + name
              + "' ("
              + oldType
              + ") into "
              + split.newType()
              + " and door control '"
              + group.control().get("name").asText()
              + "'");
    }
    groups.values().forEach(CombinedDoorPrefabMigration::applyLifecycle);
    return result;
  }

  /**
   * Converts a combined record into its standalone form in place and describes its door.
   *
   * @return the split description, or null if the record is not a combined door prefab
   */
  private static Split split(JsonNode node) {
    if (!(node instanceof ObjectNode record)) return null;
    JsonNode name = record.get("name");
    JsonNode type = record.get("type");
    if (name == null || !name.isTextual() || type == null || !type.isTextual()) return null;
    if (!(record.get("properties") instanceof ObjectNode properties)) return null;

    Split split =
        switch (type.asText()) {
          case "door-keypad" -> {
            renameProperty(properties, "keypadPosition", "position");
            yield new Split("keypad", door(properties, 0, 0), true, links(OPEN, "correct-code"));
          }
          case "door-text-keypad" -> {
            renameProperty(properties, "keypadPosition", "position");
            yield new Split(
                "text-keypad", door(properties, 0, 0), true, links(OPEN, "correct-answer"));
          }
          case "door-lever" -> {
            renameProperty(properties, "leverPosition", "position");
            boolean openAtStart = bool(properties.remove("open"), false);
            boolean revertable = bool(properties.get("revertable"), true);
            Map<String, String> links = links(openAtStart ? CLOSE : OPEN, "switched-on");
            if (revertable) links.put(openAtStart ? OPEN : CLOSE, "switched-off");
            yield new Split("lever", door(properties, 0, 0), !openAtStart, links);
          }
          case "pressure-plate" ->
              new Split(
                  "pressure-plate",
                  door(properties, 1, 0),
                  true,
                  links(OPEN, "pressed", CLOSE, "released"));
          default -> null;
        };
    if (split != null) properties.remove("doorPosition");
    return split;
  }

  private static ObjectNode doorControl(String name, Split split) {
    ObjectNode properties = NODES.objectNode();
    properties.set("doorPosition", split.door().node());
    properties.put("closedAtStart", split.closedAtStart());
    ObjectNode control = NODES.objectNode();
    control.put("name", name);
    control.put("type", "door-control");
    control.set("properties", properties);
    control.set("inputs", NODES.objectNode());
    return control;
  }

  private static void addLink(ObjectNode control, String port, String source, String sourcePort) {
    ObjectNode link = NODES.objectNode();
    link.put("source", source);
    link.put("port", sourcePort);
    ((ObjectNode) control.get("inputs")).withArrayProperty(port).add(link);
  }

  private static void applyLifecycle(DoorGroup group) {
    ObjectNode control = group.control();
    boolean anyActive = group.sources().stream().anyMatch(CombinedDoorPrefabMigration::isActive);
    if (!anyActive) control.put("active", false);
    ObjectNode inputs = (ObjectNode) control.get("inputs");
    if (group.sources().size() == 1) {
      // A single source is replaced as a whole, so the door follows its lifecycle as before.
      JsonNode sourceInputs = group.sources().getFirst().get("inputs");
      for (String port : LIFECYCLE_PORTS) {
        JsonNode links = sourceInputs == null ? null : sourceInputs.get(port);
        if (links instanceof ArrayNode array) inputs.set(port, array.deepCopy());
      }
    } else if (!anyActive) {
      // Shared door of inactive sources: control it as soon as any of them is activated.
      for (ObjectNode source : group.sources()) {
        JsonNode sourceInputs = source.get("inputs");
        JsonNode links = sourceInputs == null ? null : sourceInputs.get(Prefab.ACTIVATE.key());
        if (links instanceof ArrayNode array) {
          array.forEach(
              link -> inputs.withArrayProperty(Prefab.ACTIVATE.key()).add(link.deepCopy()));
        }
      }
    }
  }

  private static boolean isActive(ObjectNode record) {
    JsonNode active = record.get("active");
    return active == null || !active.isBoolean() || active.booleanValue();
  }

  private static String controlName(String sourceName, DoorKey key, Set<String> names) {
    int slash = sourceName.lastIndexOf('/');
    String group = slash < 0 ? "" : sourceName.substring(0, slash + 1);
    String base = group + "door-" + coordinate(key.x()) + "-" + coordinate(key.y());
    String name = base;
    for (int index = 2; names.contains(name); index++) name = base + "-" + index;
    names.add(name);
    return name;
  }

  private static String coordinate(double value) {
    return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
  }

  private static void renameProperty(ObjectNode properties, String from, String to) {
    JsonNode value = properties.remove(from);
    if (value != null && !properties.has(to)) properties.set(to, value);
  }

  private static Door door(ObjectNode properties, double defaultX, double defaultY) {
    JsonNode node = properties.get("doorPosition");
    if (node instanceof ObjectNode point
        && point.get("x") != null
        && point.get("x").isNumber()
        && point.get("y") != null
        && point.get("y").isNumber()) {
      return new Door(point.get("x").doubleValue(), point.get("y").doubleValue());
    }
    return new Door(defaultX, defaultY);
  }

  /** Door Control input port to source event port, in insertion order. */
  private static Map<String, String> links(String... portPairs) {
    Map<String, String> links = new LinkedHashMap<>();
    for (int index = 0; index < portPairs.length; index += 2) {
      links.put(portPairs[index], portPairs[index + 1]);
    }
    return links;
  }

  private static boolean bool(JsonNode node, boolean fallback) {
    return node != null && node.isBoolean() ? node.booleanValue() : fallback;
  }

  private record Door(double x, double y) {
    ObjectNode node() {
      ObjectNode point = NODES.objectNode();
      point.put("x", x);
      point.put("y", y);
      return point;
    }
  }

  private record Split(
      String newType, Door door, boolean closedAtStart, Map<String, String> links) {}

  private record DoorKey(double x, double y, boolean closedAtStart) {}

  private record DoorGroup(ObjectNode control, List<ObjectNode> sources) {
    DoorGroup(ObjectNode control) {
      this(control, new ArrayList<>());
    }
  }
}
