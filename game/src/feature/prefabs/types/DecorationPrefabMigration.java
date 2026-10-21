package feature.prefabs.types;

import feature.prefabs.Prefab;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Splits the combined decoration prefabs of level format version 5 and older into a {@link
 * DecorationInteractionPrefab} and a linked trigger prefab.
 *
 * <table>
 *   <caption>Migrated types</caption>
 *   <tr><th>Old type</th><th>New type</th><th>Trigger link</th></tr>
 *   <tr><td>deco-dialog</td><td>dialog-trigger</td><td>Show Dialog ← Interacted</td></tr>
 *   <tr><td>deco-image</td><td>show-image</td><td>Show Image ← Interacted</td></tr>
 *   <tr><td>bookshelf-image</td><td>show-image</td><td>Show Image ← Interacted</td></tr>
 * </table>
 *
 * <p>The trigger keeps the name of the old instance and owns all of its old ports, so existing
 * links and level handlers looking it up by name keep working. Its region is collapsed to a single
 * point below the decoration, so it is only shown through the link. The decoration is named {@code
 * <name>-decoration} and takes over the active flag and the lifecycle links (Activate, Deactivate,
 * Reset) of the old instance.
 *
 * <p>The migration works on the JSON records, so the old types do not have to be registered.
 */
public final class DecorationPrefabMigration {

  private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
  private static final List<String> LIFECYCLE_PORTS =
      List.of(Prefab.ACTIVATE.key(), Prefab.DEACTIVATE.key(), Prefab.RESET.key());
  private static final double TRIGGER_OFFSET_Y = -1;

  private DecorationPrefabMigration() {}

  /**
   * Returns the prefab records with all combined decoration prefabs split. Records of other types
   * and malformed records are returned unchanged.
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
    for (JsonNode node : records) {
      if (!isCombined(node)) {
        result.add(node);
        continue;
      }
      ObjectNode trigger = (ObjectNode) node;
      ObjectNode properties = (ObjectNode) trigger.get("properties");
      String name = trigger.get("name").asText();
      String oldType = trigger.get("type").asText();
      boolean dialog = oldType.equals("deco-dialog");

      String decoName = uniqueName(name + "-decoration", names);
      result.add(decoration(decoName, trigger, properties, oldType));

      double[] position = point(properties.remove("position"));
      properties.remove("deco");
      properties.remove("scale");
      ObjectNode corner = NODES.objectNode();
      corner.put("x", position[0]);
      corner.put("y", position[1] + TRIGGER_OFFSET_Y);
      ObjectNode region = NODES.objectNode();
      region.set("bottomLeft", corner);
      region.set("topRight", corner.deepCopy());
      properties.set("region", region);
      if (!dialog) properties.put("repetition", "Repeatable");
      String newType = dialog ? "dialog-trigger" : "show-image";
      trigger.put("type", newType);

      ObjectNode link = NODES.objectNode();
      link.put("source", decoName);
      link.put("port", DecorationInteractionPrefab.INTERACTED.key());
      inputs(trigger)
          .withArrayProperty(
              dialog ? DialogTriggerPrefab.SHOW_DIALOG.key() : ShowImagePrefab.SHOW_IMAGE.key())
          .add(link);
      result.add(trigger);

      log.accept(
          "Split '"
              + name
              + "' ("
              + oldType
              + ") into "
              + newType
              + " and decoration interaction '"
              + decoName
              + "'");
    }
    return result;
  }

  private static boolean isCombined(JsonNode node) {
    if (!(node instanceof ObjectNode record)) return false;
    JsonNode name = record.get("name");
    JsonNode type = record.get("type");
    if (name == null || !name.isTextual() || type == null || !type.isTextual()) return false;
    if (!(record.get("properties") instanceof ObjectNode)) return false;
    JsonNode inputs = record.get("inputs");
    if (inputs != null && !(inputs instanceof ObjectNode)) return false;
    return switch (type.asText()) {
      case "deco-dialog", "deco-image", "bookshelf-image" -> true;
      default -> false;
    };
  }

  private static ObjectNode decoration(
      String name, ObjectNode source, ObjectNode sourceProperties, String oldType) {
    ObjectNode properties = NODES.objectNode();
    JsonNode position = sourceProperties.get("position");
    if (position != null) properties.set("position", position.deepCopy());
    if (oldType.equals("bookshelf-image")) {
      properties.put("deco", "BookshelfLarge");
    } else {
      JsonNode deco = sourceProperties.get("deco");
      JsonNode scale = sourceProperties.get("scale");
      if (deco != null) properties.set("deco", deco.deepCopy());
      if (scale != null) properties.set("scale", scale.deepCopy());
    }

    ObjectNode decoration = NODES.objectNode();
    decoration.put("name", name);
    decoration.put("type", "deco-interaction");
    decoration.set("properties", properties);
    JsonNode active = source.get("active");
    if (active != null) decoration.set("active", active.deepCopy());
    ObjectNode inputs = NODES.objectNode();
    JsonNode sourceInputs = source.get("inputs");
    for (String port : LIFECYCLE_PORTS) {
      JsonNode links = sourceInputs == null ? null : sourceInputs.get(port);
      if (links instanceof ArrayNode array) inputs.set(port, array.deepCopy());
    }
    decoration.set("inputs", inputs);
    return decoration;
  }

  private static ObjectNode inputs(ObjectNode record) {
    if (record.get("inputs") instanceof ObjectNode inputs) return inputs;
    ObjectNode inputs = NODES.objectNode();
    record.set("inputs", inputs);
    return inputs;
  }

  private static double[] point(JsonNode node) {
    if (node instanceof ObjectNode point
        && point.get("x") != null
        && point.get("x").isNumber()
        && point.get("y") != null
        && point.get("y").isNumber()) {
      return new double[] {point.get("x").doubleValue(), point.get("y").doubleValue()};
    }
    return new double[] {0, 0};
  }

  private static String uniqueName(String base, Set<String> names) {
    String name = base;
    for (int index = 2; names.contains(name); index++) name = base + "-" + index;
    names.add(name);
    return name;
  }
}
