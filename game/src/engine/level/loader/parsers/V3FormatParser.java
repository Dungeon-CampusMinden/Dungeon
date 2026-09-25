package engine.level.loader.parsers;

import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.entities.deco.Deco;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabPropertyType;
import feature.prefabs.PrefabRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** Parser and serializer for the JSON-based version 3 level format. */
public final class V3FormatParser extends LevelFormatParser {

  private static final int VERSION = 3;
  private static final Set<String> ROOT_PROPERTIES =
      Set.of("version", "design", "layout", "startTiles", "namedPoints", "decorations", "prefabs");
  private static final Set<String> POINT_PROPERTIES = Set.of("x", "y");
  private static final Set<String> DECORATION_PROPERTIES = Set.of("type", "position");
  private static final Set<String> PREFAB_PROPERTIES = Set.of("name", "type", "properties");
  private static final ObjectMapper MAPPER =
      JsonMapper.builder(
              JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .build();

  @Override
  public DungeonLevel parseLevel(BufferedReader reader, String levelName) throws IOException {
    StringBuilder input = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      if (!input.isEmpty()) input.append('\n');
      input.append(line);
    }

    try {
      JsonNode parsed = MAPPER.readTree(input.toString());
      if (!(parsed instanceof ObjectNode root)) {
        throw new IllegalArgumentException("V3 level data must be a JSON object");
      }
      rejectUnknown(root, ROOT_PROPERTIES, "level");
      requireInteger(root, "version", VERSION);

      DesignLabel design = parseDesign(root);
      List<String> layoutRows = parseLayout(root);
      LevelElement[][] layout = V2FormatParser.loadLevelLayout(layoutRows);
      List<Point> startTiles = parsePointArray(requireArray(root, "startTiles"), "startTiles");
      Map<String, Point> namedPoints = parseNamedPoints(root);
      List<Tuple<Deco, Point>> decorations = parseDecorations(root);
      List<PrefabInstance> prefabs = parsePrefabs(root);

      DungeonLevel level = getLevel(levelName, layout, design, namedPoints, decorations);
      for (Point start : startTiles) {
        Tile tile =
            level
                .tileAt(start)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException("Start tile is outside the level: " + start));
        level.startTiles().add(tile);
      }
      level.prefabs().addAll(prefabs);
      return level;
    } catch (JacksonException e) {
      throw new IllegalArgumentException("Invalid V3 level JSON: " + e.getMessage(), e);
    }
  }

  @Override
  public String serializeLevel(DungeonLevel level) {
    if (level == null) {
      throw new IllegalArgumentException("Level to serialize cannot be null");
    }

    ObjectNode root = MAPPER.createObjectNode();
    root.put("version", VERSION);
    root.put("design", level.designLabel().orElse(DesignLabel.DEFAULT).name());
    root.set("layout", serializeLayout(level.layout()));
    root.set(
        "startTiles", serializePoints(level.startTiles().stream().map(Tile::position).toList()));
    root.set("namedPoints", serializeNamedPoints(level.namedPoints()));
    root.set("decorations", serializeDecorations(level.decorations()));
    root.set("prefabs", serializePrefabs(level.prefabs()));

    try {
      return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    } catch (JacksonException e) {
      throw new IllegalArgumentException("Could not serialize V3 level JSON", e);
    }
  }

  private static DesignLabel parseDesign(ObjectNode root) {
    String value = requireText(root, "design");
    try {
      return DesignLabel.valueOf(value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid design label: '" + value + "'", e);
    }
  }

  private static List<String> parseLayout(ObjectNode root) {
    ArrayNode rows = requireArray(root, "layout");
    if (rows.isEmpty()) throw new IllegalArgumentException("Level layout must not be empty");

    List<String> result = new ArrayList<>();
    int width = -1;
    for (int index = 0; index < rows.size(); index++) {
      JsonNode rowNode = rows.get(index);
      if (!rowNode.isTextual() || rowNode.asText().isEmpty()) {
        throw new IllegalArgumentException("Layout row " + index + " must be a non-empty string");
      }
      String row = rowNode.asText();
      if (width < 0) width = row.length();
      if (row.length() != width) {
        throw new IllegalArgumentException("All level layout rows must have equal length");
      }
      for (int character = 0; character < row.length(); character++) {
        getLevelElementFromChar(row.charAt(character));
      }
      result.add(row);
    }
    return result;
  }

  private static Map<String, Point> parseNamedPoints(ObjectNode root) {
    JsonNode node = require(root, "namedPoints");
    if (!(node instanceof ObjectNode object)) {
      throw new IllegalArgumentException("'namedPoints' must be a JSON object");
    }

    Map<String, Point> result = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> entry : object.properties()) {
      if (entry.getKey().isBlank()) {
        throw new IllegalArgumentException("Named point names must not be blank");
      }
      result.put(entry.getKey(), parsePoint(entry.getValue(), "namedPoints." + entry.getKey()));
    }
    return result;
  }

  private static List<Tuple<Deco, Point>> parseDecorations(ObjectNode root) {
    ArrayNode nodes = requireArray(root, "decorations");
    List<Tuple<Deco, Point>> result = new ArrayList<>();
    for (int index = 0; index < nodes.size(); index++) {
      JsonNode node = nodes.get(index);
      if (!(node instanceof ObjectNode object)) {
        throw new IllegalArgumentException("Decoration " + index + " must be a JSON object");
      }
      rejectUnknown(object, DECORATION_PROPERTIES, "decoration " + index);
      String type = requireText(object, "type");
      Deco deco;
      try {
        deco = Deco.valueOf(type);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid decoration type: '" + type + "'", e);
      }
      Point position = parsePoint(require(object, "position"), "decoration " + index + ".position");
      result.add(new Tuple<>(deco, position));
    }
    return result;
  }

  private static List<PrefabInstance> parsePrefabs(ObjectNode root) {
    ArrayNode nodes = requireArray(root, "prefabs");
    List<PrefabInstance> result = new ArrayList<>();
    Set<String> names = new HashSet<>();
    for (int index = 0; index < nodes.size(); index++) {
      JsonNode node = nodes.get(index);
      if (!(node instanceof ObjectNode object)) {
        warnSkippedPrefab(index, null, null, "record must be a JSON object");
        continue;
      }

      String name = optionalText(object, "name");
      String type = optionalText(object, "type");
      try {
        rejectUnknown(object, PREFAB_PROPERTIES, "prefab " + index);
        name = requireText(object, "name");
        if (name.isBlank()) {
          throw new IllegalArgumentException("name must not be blank");
        }
        type = requireText(object, "type");
        Prefab prefab = PrefabRegistry.require(type);
        JsonNode propertiesNode = require(object, "properties");
        if (!(propertiesNode instanceof ObjectNode propertiesObject)) {
          throw new IllegalArgumentException("properties must be a JSON object");
        }
        if (type.equals("level-hider")
            && (propertiesObject.has("firstCorner") || propertiesObject.has("secondCorner"))) {
          throw new IllegalArgumentException(
              "legacy Level Hider firstCorner/secondCorner properties are no longer supported");
        }
        for (PrefabProperty<?> property : prefab.properties()) {
          if (property.type() == PrefabPropertyType.REGION
              && !propertiesObject.has(property.key())) {
            throw new IllegalArgumentException(
                "required Region property '" + property.key() + "' is missing");
          }
        }

        Map<String, JsonNode> properties = new LinkedHashMap<>();
        propertiesObject
            .properties()
            .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
        PrefabInstance normalized = prefab.normalize(new PrefabInstance(name, type, properties));
        if (names.contains(name)) {
          throw new IllegalArgumentException("duplicate prefab name '" + name + "'");
        }
        result.add(normalized);
        names.add(name);
      } catch (IllegalArgumentException exception) {
        warnSkippedPrefab(index, name, type, exception.getMessage());
      }
    }
    return result;
  }

  private static String optionalText(ObjectNode object, String property) {
    JsonNode value = object.get(property);
    return value != null && value.isTextual() ? value.asText() : null;
  }

  private static void warnSkippedPrefab(int index, String name, String type, String reason) {
    LOGGER.warn(
        "Skipping prefab at index {} (name='{}', type='{}'): {}",
        index,
        name == null ? "<missing or invalid>" : name,
        type == null ? "<missing or invalid>" : type,
        reason == null || reason.isBlank() ? "invalid prefab record" : reason);
  }

  private static List<Point> parsePointArray(ArrayNode nodes, String fieldName) {
    List<Point> result = new ArrayList<>();
    for (int index = 0; index < nodes.size(); index++) {
      result.add(parsePoint(nodes.get(index), fieldName + "[" + index + "]"));
    }
    return result;
  }

  private static Point parsePoint(JsonNode node, String path) {
    if (!(node instanceof ObjectNode object)) {
      throw new IllegalArgumentException("'" + path + "' must be a JSON object");
    }
    rejectUnknown(object, POINT_PROPERTIES, path);
    JsonNode x = require(object, "x");
    JsonNode y = require(object, "y");
    if (!x.isNumber() || !y.isNumber()) {
      throw new IllegalArgumentException("'" + path + "' must contain numeric x and y values");
    }
    float xValue = x.floatValue();
    float yValue = y.floatValue();
    if (!Float.isFinite(xValue) || !Float.isFinite(yValue)) {
      throw new IllegalArgumentException("'" + path + "' must contain finite coordinates");
    }
    return new Point(xValue, yValue);
  }

  private static ArrayNode serializeLayout(Tile[][] layout) {
    if (layout.length == 0 || layout[0].length == 0) {
      throw new IllegalArgumentException("Level layout must not be empty");
    }
    ArrayNode rows = MAPPER.createArrayNode();
    for (int y = layout.length - 1; y >= 0; y--) {
      StringBuilder row = new StringBuilder(layout[y].length);
      for (Tile tile : layout[y]) {
        if (tile == null) throw new IllegalArgumentException("Level layout contains a null tile");
        row.append(getCharFromLevelElement(tile.levelElement()));
      }
      rows.add(row.toString());
    }
    return rows;
  }

  private static ArrayNode serializePoints(List<Point> points) {
    ArrayNode result = MAPPER.createArrayNode();
    points.forEach(point -> result.add(serializePoint(point)));
    return result;
  }

  private static ObjectNode serializeNamedPoints(Map<String, Point> points) {
    ObjectNode result = MAPPER.createObjectNode();
    points.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> result.set(entry.getKey(), serializePoint(entry.getValue())));
    return result;
  }

  private static ArrayNode serializeDecorations(List<Tuple<Deco, Point>> decorations) {
    ArrayNode result = MAPPER.createArrayNode();
    for (Tuple<Deco, Point> decoration : decorations) {
      ObjectNode node = MAPPER.createObjectNode();
      node.put("type", decoration.a().name());
      node.set("position", serializePoint(decoration.b()));
      result.add(node);
    }
    return result;
  }

  private static ArrayNode serializePrefabs(List<PrefabInstance> instances) {
    ArrayNode result = MAPPER.createArrayNode();
    Set<String> names = new HashSet<>();
    for (PrefabInstance source : instances) {
      if (!names.add(source.name())) {
        throw new IllegalArgumentException(
            "Duplicate prefab instance name: '" + source.name() + "'");
      }
      PrefabInstance instance = PrefabRegistry.require(source.type()).normalize(source);
      ObjectNode node = MAPPER.createObjectNode();
      node.put("name", instance.name());
      node.put("type", instance.type());
      ObjectNode properties = MAPPER.createObjectNode();
      instance.properties().forEach(properties::set);
      node.set("properties", properties);
      result.add(node);
    }
    return result;
  }

  private static ObjectNode serializePoint(Point point) {
    if (!Float.isFinite(point.x()) || !Float.isFinite(point.y())) {
      throw new IllegalArgumentException("Point coordinates must be finite");
    }
    ObjectNode node = MAPPER.createObjectNode();
    node.put("x", point.x());
    node.put("y", point.y());
    return node;
  }

  private static JsonNode require(ObjectNode object, String property) {
    JsonNode value = object.get(property);
    if (value == null || value.isNull()) {
      throw new IllegalArgumentException("Missing required property '" + property + "'");
    }
    return value;
  }

  private static String requireText(ObjectNode object, String property) {
    JsonNode value = require(object, property);
    if (!value.isTextual()) {
      throw new IllegalArgumentException("'" + property + "' must be a string");
    }
    return value.asText();
  }

  private static ArrayNode requireArray(ObjectNode object, String property) {
    JsonNode value = require(object, property);
    if (!(value instanceof ArrayNode array)) {
      throw new IllegalArgumentException("'" + property + "' must be an array");
    }
    return array;
  }

  private static void requireInteger(ObjectNode object, String property, int expected) {
    JsonNode value = require(object, property);
    if (!value.isIntegralNumber() || value.intValue() != expected) {
      throw new IllegalArgumentException("'" + property + "' must be the integer " + expected);
    }
  }

  private static void rejectUnknown(
      ObjectNode object, Set<String> allowedProperties, String description) {
    for (String property : object.propertyNames()) {
      if (!allowedProperties.contains(property)) {
        throw new IllegalArgumentException("Unknown property '" + property + "' in " + description);
      }
    }
  }
}
