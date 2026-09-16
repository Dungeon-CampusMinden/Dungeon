package feature.prefabs;

import engine.utils.Point;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Typed, editor-readable property definition for a prefab.
 *
 * @param <T> decoded Java value type
 */
public abstract class PrefabProperty<T> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final String key;
  private final String displayName;
  private final PrefabPropertyType type;
  private final T defaultValue;

  protected PrefabProperty(
      String key, String displayName, PrefabPropertyType type, T defaultValue) {
    this.key = requireText(key, "key");
    this.displayName = requireText(displayName, "displayName");
    this.type = Objects.requireNonNull(type, "type");
    this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
  }

  /**
   * Returns the stable serialized property key.
   *
   * @return property key
   */
  public final String key() {
    return key;
  }

  /**
   * Returns the label for generated editor controls.
   *
   * @return display label
   */
  public final String displayName() {
    return displayName;
  }

  /**
   * Returns the type used to choose an editor control.
   *
   * @return property type
   */
  public final PrefabPropertyType type() {
    return type;
  }

  /**
   * Returns the default used for new or omitted properties.
   *
   * @return default value
   */
  public final T defaultValue() {
    return defaultValue;
  }

  /**
   * Decodes and validates a serialized value.
   *
   * @param node JSON property value
   * @return typed value
   */
  public abstract T decode(JsonNode node);

  /**
   * Encodes a typed value.
   *
   * @param mapper JSON mapper
   * @param value typed value
   * @return JSON property value
   */
  public abstract JsonNode encode(ObjectMapper mapper, T value);

  /**
   * Reads this property from an instance.
   *
   * @param instance prefab instance
   * @return decoded value or the property default when omitted
   */
  public final T get(PrefabInstance instance) {
    JsonNode value = instance.properties().get(key);
    return value == null ? defaultValue : decode(value);
  }

  /**
   * Creates an updated prefab instance containing a typed property value.
   *
   * <p>This is the mutation hook used by generated editor controls.
   *
   * @param instance source instance
   * @param value new typed value
   * @return updated immutable instance
   */
  public final PrefabInstance set(PrefabInstance instance, T value) {
    return instance.withProperty(key, encode(MAPPER, value));
  }

  /**
   * Returns a normalized JSON representation of the supplied node.
   *
   * @param mapper JSON mapper
   * @param node supplied value, or null to use the default
   * @return normalized serialized value
   */
  public final JsonNode normalize(ObjectMapper mapper, JsonNode node) {
    return encode(mapper, node == null ? defaultValue : decode(node));
  }

  /**
   * Creates a string or asset-path property.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default value
   * @param validator value validator
   * @return string property descriptor
   */
  public static PrefabProperty<String> string(
      String key, String displayName, String defaultValue, Predicate<String> validator) {
    Objects.requireNonNull(validator, "validator");
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.STRING, defaultValue) {
      @Override
      public String decode(JsonNode node) {
        if (!node.isTextual()) throw invalid(key, "must be a string");
        String value = node.asText();
        if (!validator.test(value)) throw invalid(key, "contains an invalid value");
        return value;
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, String value) {
        if (!validator.test(value)) throw invalid(key, "contains an invalid value");
        return mapper.getNodeFactory().textNode(value);
      }
    };
  }

  /**
   * Creates a bounded integer property.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default value
   * @param minimum minimum accepted value
   * @param maximum maximum accepted value
   * @return integer property descriptor
   */
  public static PrefabProperty<Integer> integer(
      String key, String displayName, int defaultValue, int minimum, int maximum) {
    if (minimum > maximum) throw new IllegalArgumentException("minimum must not exceed maximum");
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.INTEGER, defaultValue) {
      @Override
      public Integer decode(JsonNode node) {
        if (!node.isIntegralNumber() || !node.canConvertToInt()) {
          throw invalid(key, "must be an integer");
        }
        return validate(node.intValue());
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Integer value) {
        return mapper.getNodeFactory().numberNode(validate(value));
      }

      @Override
      public Optional<Number> minimum() {
        return Optional.of(minimum);
      }

      @Override
      public Optional<Number> maximum() {
        return Optional.of(maximum);
      }

      private int validate(int value) {
        if (value < minimum || value > maximum) {
          throw invalid(key, "must be between " + minimum + " and " + maximum);
        }
        return value;
      }
    };
  }

  /**
   * Creates a bounded finite float property.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default value
   * @param minimum minimum accepted value
   * @param maximum maximum accepted value
   * @return float property descriptor
   */
  public static PrefabProperty<Float> floating(
      String key, String displayName, float defaultValue, float minimum, float maximum) {
    if (!Float.isFinite(minimum) || !Float.isFinite(maximum) || minimum > maximum) {
      throw new IllegalArgumentException("invalid float property bounds");
    }
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.FLOAT, defaultValue) {
      @Override
      public Float decode(JsonNode node) {
        if (!node.isNumber()) throw invalid(key, "must be a number");
        return validate(node.floatValue());
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Float value) {
        return mapper.getNodeFactory().numberNode(validate(value));
      }

      @Override
      public Optional<Number> minimum() {
        return Optional.of(minimum);
      }

      @Override
      public Optional<Number> maximum() {
        return Optional.of(maximum);
      }

      private float validate(float value) {
        if (!Float.isFinite(value) || value < minimum || value > maximum) {
          throw invalid(key, "must be finite and between " + minimum + " and " + maximum);
        }
        return value;
      }
    };
  }

  /**
   * Creates a boolean property.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default value
   * @return boolean property descriptor
   */
  public static PrefabProperty<Boolean> bool(String key, String displayName, boolean defaultValue) {
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.BOOLEAN, defaultValue) {
      @Override
      public Boolean decode(JsonNode node) {
        if (!node.isBoolean()) throw invalid(key, "must be a boolean");
        return node.booleanValue();
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Boolean value) {
        return mapper.getNodeFactory().booleanNode(value);
      }
    };
  }

  /**
   * Creates a string-backed enum/select property.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default value
   * @param values selectable values
   * @return selection property descriptor
   */
  public static PrefabProperty<String> selection(
      String key, String displayName, String defaultValue, List<String> values) {
    List<String> allowedValues = List.copyOf(values);
    if (allowedValues.isEmpty() || !allowedValues.contains(defaultValue)) {
      throw new IllegalArgumentException("selection values must contain the default");
    }
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.ENUM, defaultValue) {
      @Override
      public String decode(JsonNode node) {
        if (!node.isTextual()) throw invalid(key, "must be a string");
        return validate(node.asText());
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, String value) {
        return mapper.getNodeFactory().textNode(validate(value));
      }

      @Override
      public List<String> choices() {
        return allowedValues;
      }

      private String validate(String value) {
        if (!allowedValues.contains(value)) {
          throw invalid(key, "must be one of " + allowedValues);
        }
        return value;
      }
    };
  }

  /**
   * Creates a finite world-point property.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default point
   * @return point property descriptor
   */
  public static PrefabProperty<Point> point(String key, String displayName, Point defaultValue) {
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.POINT, defaultValue) {
      @Override
      public Point decode(JsonNode node) {
        if (!node.isObject()
            || node.size() != 2
            || !node.has("x")
            || !node.has("y")
            || !node.get("x").isNumber()
            || !node.get("y").isNumber()) {
          throw invalid(key, "must be an object containing numeric x and y properties");
        }
        return validate(new Point(node.get("x").floatValue(), node.get("y").floatValue()));
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Point value) {
        Point point = validate(value);
        ObjectNode node = mapper.createObjectNode();
        node.put("x", point.x());
        node.put("y", point.y());
        return node;
      }

      private Point validate(Point point) {
        if (!Float.isFinite(point.x()) || !Float.isFinite(point.y())) {
          throw invalid(key, "must contain finite coordinates");
        }
        return point;
      }
    };
  }

  /**
   * Selectable values for enum properties.
   *
   * @return empty for non-enum properties
   */
  public List<String> choices() {
    return List.of();
  }

  /**
   * Optional numeric lower bound for generated editor controls.
   *
   * @return lower bound, if this is a bounded numeric property
   */
  public Optional<Number> minimum() {
    return Optional.empty();
  }

  /**
   * Optional numeric upper bound for generated editor controls.
   *
   * @return upper bound, if this is a bounded numeric property
   */
  public Optional<Number> maximum() {
    return Optional.empty();
  }

  private static String requireText(String value, String field) {
    Objects.requireNonNull(value, field);
    if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    return value;
  }

  private static IllegalArgumentException invalid(String key, String message) {
    return new IllegalArgumentException("Prefab property '" + key + "' " + message);
  }
}
