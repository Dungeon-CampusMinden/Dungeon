package feature.prefabs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * JSON form of prefab instance records, shared by level files and network messages.
 *
 * <pre>{@code
 * {
 *   "name": "vault/door",
 *   "type": "door-control",
 *   "properties": { ... },
 *   "active": false,
 *   "inputs": { "open": [ { "source": "vault/keypad", "port": "correct-code" } ] }
 * }
 * }</pre>
 *
 * <p>{@code active} is omitted while it is {@code true} and {@code inputs} while it is empty, so
 * records without links look like they did before links existed.
 */
public final class PrefabInstanceCodec {

  /** Keys allowed in a serialized record. */
  public static final Set<String> RECORD_KEYS =
      Set.of("name", "type", "properties", "active", "inputs");

  private static final Set<String> LINK_KEYS = Set.of("source", "port", "alias");

  private PrefabInstanceCodec() {}

  /**
   * Serializes a normalized instance.
   *
   * @param mapper JSON mapper
   * @param instance normalized instance
   * @return JSON record
   */
  public static ObjectNode encode(ObjectMapper mapper, PrefabInstance instance) {
    ObjectNode node = mapper.createObjectNode();
    node.put("name", instance.name());
    node.put("type", instance.type());
    ObjectNode properties = mapper.createObjectNode();
    instance.properties().forEach(properties::set);
    node.set("properties", properties);
    if (!instance.active()) node.put("active", false);
    if (!instance.inputs().isEmpty()) {
      ObjectNode inputs = mapper.createObjectNode();
      instance
          .inputs()
          .forEach(
              (port, links) -> {
                ArrayNode array = mapper.createArrayNode();
                for (PrefabLink link : links) {
                  ObjectNode linkNode = mapper.createObjectNode();
                  linkNode.put("source", link.source());
                  linkNode.put("port", link.port());
                  if (link.alias() != null) linkNode.put("alias", link.alias());
                  array.add(linkNode);
                }
                inputs.set(port, array);
              });
      node.set("inputs", inputs);
    }
    return node;
  }

  /**
   * Reads the {@code active} flag of a record.
   *
   * @param record JSON record
   * @return the flag, {@code true} if it is missing
   */
  public static boolean decodeActive(ObjectNode record) {
    JsonNode active = record.get("active");
    if (active == null || active.isNull()) return true;
    if (!active.isBoolean()) throw new IllegalArgumentException("'active' must be a boolean");
    return active.booleanValue();
  }

  /**
   * Reads the {@code inputs} of a record. Malformed links and links of unknown input ports are
   * skipped and reported, so one broken link does not discard the whole instance.
   *
   * @param prefab definition of the record's prefab type
   * @param record JSON record
   * @param warnings receives a description of each skipped link
   * @return links by input port key
   */
  public static Map<String, List<PrefabLink>> decodeInputs(
      Prefab prefab, ObjectNode record, Consumer<String> warnings) {
    JsonNode inputsNode = record.get("inputs");
    Map<String, List<PrefabLink>> result = new LinkedHashMap<>();
    if (inputsNode == null || inputsNode.isNull()) return result;
    if (!(inputsNode instanceof ObjectNode inputs)) {
      throw new IllegalArgumentException("'inputs' must be a JSON object");
    }
    for (Map.Entry<String, JsonNode> entry : inputs.properties()) {
      String port = entry.getKey();
      boolean known = prefab.port(port).filter(p -> p.kind().isInput()).isPresent();
      if (!known) {
        if (!prefab.isLegacyPort(port)) {
          warnings.accept("Skipping links of unknown input port '" + port + "'");
        }
        continue;
      }
      if (!(entry.getValue() instanceof ArrayNode array)) {
        warnings.accept("Skipping links of input port '" + port + "': must be an array");
        continue;
      }
      List<PrefabLink> links = new ArrayList<>();
      for (int index = 0; index < array.size(); index++) {
        try {
          links.add(decodeLink(array.get(index)));
        } catch (IllegalArgumentException exception) {
          warnings.accept(
              "Skipping link "
                  + index
                  + " of input port '"
                  + port
                  + "': "
                  + exception.getMessage());
        }
      }
      result.put(port, links);
    }
    return result;
  }

  /**
   * Reads a complete record, e.g. from a network message.
   *
   * @param record JSON record
   * @param warnings receives a description of each skipped link
   * @return normalized instance
   */
  public static PrefabInstance decode(ObjectNode record, Consumer<String> warnings) {
    JsonNode name = record.get("name");
    JsonNode type = record.get("type");
    if (name == null || !name.isTextual() || type == null || !type.isTextual()) {
      throw new IllegalArgumentException("Prefab record needs a string name and type");
    }
    Prefab prefab = PrefabRegistry.require(type.asText());
    Map<String, JsonNode> properties = new LinkedHashMap<>();
    JsonNode propertiesNode = record.get("properties");
    if (propertiesNode instanceof ObjectNode object) {
      object.properties().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
    }
    return prefab.normalize(
        new PrefabInstance(
            name.asText(),
            type.asText(),
            properties,
            decodeActive(record),
            decodeInputs(prefab, record, warnings)));
  }

  private static PrefabLink decodeLink(JsonNode node) {
    if (!(node instanceof ObjectNode object)) {
      throw new IllegalArgumentException("link must be a JSON object");
    }
    for (String key : object.propertyNames()) {
      if (!LINK_KEYS.contains(key)) {
        throw new IllegalArgumentException("unknown link property '" + key + "'");
      }
    }
    JsonNode source = object.get("source");
    JsonNode port = object.get("port");
    JsonNode alias = object.get("alias");
    if (source == null || !source.isTextual() || port == null || !port.isTextual()) {
      throw new IllegalArgumentException("link needs a string source and port");
    }
    if (alias != null && !alias.isNull() && !alias.isTextual()) {
      throw new IllegalArgumentException("link alias must be a string");
    }
    return new PrefabLink(
        source.asText(), port.asText(), alias == null || alias.isNull() ? null : alias.asText());
  }
}
