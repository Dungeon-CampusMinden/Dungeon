package feature.prefabs;

import com.badlogic.gdx.graphics.Color;
import engine.utils.Point;
import engine.utils.Vector2;
import java.util.List;
import java.util.Locale;
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
  private static final Point ZERO_POINT = new Point(0f, 0f);

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
   * Returns the visual offset used for this property's editor feedback point.
   *
   * @return editor feedback offset, or zero for non-point properties
   */
  public Point editorFeedbackOffset() {
    return ZERO_POINT;
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
   * Creates a numeric slider property using the default range of zero to one and continuous
   * precision.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default value
   * @return slider property descriptor
   */
  public static PrefabProperty<Float> numberSlider(
      String key, String displayName, float defaultValue) {
    return numberSlider(key, displayName, defaultValue, 0f, 1f, 0f);
  }

  /**
   * Creates a bounded finite float property intended to be edited with a slider.
   *
   * <p>Values are anchored to {@code minimum} when quantized. The maximum is also a valid endpoint
   * even when it is not an exact multiple of {@code step}.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default value (clamped and quantized to the slider range)
   * @param minimum minimum slider value
   * @param maximum maximum slider value
   * @param step quantization step, or zero for continuous values
   * @return slider property descriptor
   */
  public static PrefabProperty<Float> numberSlider(
      String key,
      String displayName,
      float defaultValue,
      float minimum,
      float maximum,
      float step) {
    if (!Float.isFinite(minimum)
        || !Float.isFinite(maximum)
        || !Float.isFinite(step)
        || minimum > maximum
        || step < 0f) {
      throw new IllegalArgumentException("invalid number slider bounds or step");
    }
    if (!Float.isFinite(defaultValue)) {
      throw new IllegalArgumentException("number slider default must be finite");
    }
    float normalizedDefault = quantize(clamp(defaultValue, minimum, maximum), minimum, maximum, step);
    return new PrefabProperty<>(
        key, displayName, PrefabPropertyType.NUMBER_SLIDER, normalizedDefault) {
      @Override
      public Float decode(JsonNode node) {
        if (!node.isNumber()) throw invalid(key, "must be a number");
        float value = node.floatValue();
        if (!Float.isFinite(value) || value < minimum || value > maximum) {
          throw invalid(key, "must be finite and between " + minimum + " and " + maximum);
        }
        // Quantization is the one intentional normalization performed on loaded values.
        return quantize(value, minimum, maximum, step);
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Float value) {
        if (value == null || !Float.isFinite(value)) {
          throw invalid(key, "must be finite");
        }
        return mapper.getNodeFactory()
            .numberNode(quantize(clamp(value, minimum, maximum), minimum, maximum, step));
      }

      @Override
      public Optional<Number> minimum() {
        return Optional.of(minimum);
      }

      @Override
      public Optional<Number> maximum() {
        return Optional.of(maximum);
      }

      @Override
      public Optional<Number> step() {
        return Optional.of(step);
      }
    };
  }

  private static float clamp(float value, float minimum, float maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }

  private static float quantize(float value, float minimum, float maximum, float step) {
    if (step == 0f || minimum == maximum) return value;

    double offset = (double) value - minimum;
    double stepCount = Math.floor(offset / step + 0.5d);
    double quantized = (double) minimum + stepCount * step;
    // Include maximum as a terminal stop when the range is not divisible by step.
    quantized = Math.max(minimum, Math.min(maximum, quantized));
    double toMaximum = Math.abs((double) maximum - value);
    double toQuantized = Math.abs(quantized - value);
    if (toMaximum < toQuantized) return maximum;
    return (float) quantized;
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
    return point(key, displayName, defaultValue, ZERO_POINT);
  }

  /**
   * Creates a finite world-point property with an editor feedback offset.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default point
   * @param editorFeedbackOffset visual offset for editor feedback markers
   * @return point property descriptor
   */
  public static PrefabProperty<Point> point(
      String key, String displayName, Point defaultValue, Point editorFeedbackOffset) {
    Objects.requireNonNull(editorFeedbackOffset, "editorFeedbackOffset");
    if (!Float.isFinite(editorFeedbackOffset.x()) || !Float.isFinite(editorFeedbackOffset.y())) {
      throw new IllegalArgumentException("editor feedback offset must be finite");
    }
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.POINT, defaultValue) {
      @Override
      public Point editorFeedbackOffset() {
        return editorFeedbackOffset;
      }

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
   * Creates a finite, normalized world-region property.
   *
   * <p>The serialized form is an object with {@code bottomLeft} and {@code topRight} point
   * objects, each containing numeric {@code x} and {@code y} coordinates. Reversed corners are
   * normalized; zero-area regions are valid.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default region
   * @return region property descriptor
   */
  public static PrefabProperty<Region> region(
      String key, String displayName, Region defaultValue) {
    Objects.requireNonNull(defaultValue, "defaultValue");
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.REGION, defaultValue) {
      @Override
      public Region decode(JsonNode node) {
        if (node == null
            || !node.isObject()
            || node.size() != 2
            || !node.has("bottomLeft")
            || !node.has("topRight")) {
          throw invalid(key, "must contain bottomLeft and topRight point objects");
        }
        return validate(
            new Region(
                decodePoint(node.get("bottomLeft"), key),
                decodePoint(node.get("topRight"), key)));
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Region value) {
        Region region = validate(value);
        ObjectNode node = mapper.createObjectNode();
        node.set("bottomLeft", encodePoint(mapper, region.bottomLeft()));
        node.set("topRight", encodePoint(mapper, region.topRight()));
        return node;
      }

      private Region validate(Region value) {
        if (value == null) throw invalid(key, "must not be null");
        try {
          return new Region(value.bottomLeft(), value.topRight());
        } catch (IllegalArgumentException exception) {
          throw invalid(key, "must contain finite coordinates");
        }
      }
    };
  }

  /**
   * Creates a finite vector property.
   *
   * <p>The serialized form is an object containing numeric {@code x} and {@code y} components.
   * Unlike a point, a vector is not a world position and is not translated with a level.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default vector
   * @return vector property descriptor
   */
  public static PrefabProperty<Vector2> vector2(
      String key, String displayName, Vector2 defaultValue) {
    Vector2 validatedDefault = validateVector(key, defaultValue);
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.VECTOR2, validatedDefault) {
      @Override
      public Vector2 decode(JsonNode node) {
        Point point = decodePoint(node, key);
        return validateVector(key, Vector2.of(point.x(), point.y()));
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Vector2 value) {
        return encodeVector(mapper, validateVector(key, value));
      }
    };
  }

  /**
   * Creates a color property encoded as eight hexadecimal RGBA digits.
   *
   * <p>Input accepts exactly eight hexadecimal digits, optionally prefixed with {@code #}, in
   * either case. Output is always eight uppercase digits without a prefix. Typed colors must have
   * finite red, green, blue, and alpha channels in the inclusive range from zero to one.
   *
   * @param key serialized key
   * @param displayName editor label
   * @param defaultValue default color
   * @return color property descriptor
   */
  public static PrefabProperty<Color> color(
      String key, String displayName, Color defaultValue) {
    Color validatedDefault = new Color(validateColor(key, defaultValue));
    return new PrefabProperty<>(key, displayName, PrefabPropertyType.COLOR, validatedDefault) {
      @Override
      public Color decode(JsonNode node) {
        if (node == null || !node.isTextual()) {
          throw invalid(key, "must be an eight-digit RGBA hexadecimal string");
        }
        String encoded = node.asText();
        if (encoded.startsWith("#")) encoded = encoded.substring(1);
        if (encoded.length() != 8 || !isAsciiHex(encoded)) {
          throw invalid(key, "must be an eight-digit RGBA hexadecimal string");
        }
        int red = Integer.parseInt(encoded.substring(0, 2), 16);
        int green = Integer.parseInt(encoded.substring(2, 4), 16);
        int blue = Integer.parseInt(encoded.substring(4, 6), 16);
        int alpha = Integer.parseInt(encoded.substring(6, 8), 16);
        return new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
      }

      @Override
      public JsonNode encode(ObjectMapper mapper, Color value) {
        Color color = validateColor(key, value);
        return mapper
            .getNodeFactory()
            .textNode(
                String.format(
                    Locale.ROOT,
                    "%02X%02X%02X%02X",
                    Math.round(color.r * 255f),
                    Math.round(color.g * 255f),
                    Math.round(color.b * 255f),
                    Math.round(color.a * 255f)));
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

  /**
   * Optional step size for generated numeric slider controls.
   *
   * @return slider step, if this is a slider property
   */
  public Optional<Number> step() {
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

  private static Point decodePoint(JsonNode node, String key) {
    if (node == null
        || !node.isObject()
        || node.size() != 2
        || !node.has("x")
        || !node.has("y")
        || !node.get("x").isNumber()
        || !node.get("y").isNumber()) {
      throw invalid(key, "must be an object containing numeric x and y properties");
    }
    Point point = new Point(node.get("x").floatValue(), node.get("y").floatValue());
    if (!Float.isFinite(point.x()) || !Float.isFinite(point.y())) {
      throw invalid(key, "must contain finite coordinates");
    }
    return point;
  }

  private static ObjectNode encodePoint(ObjectMapper mapper, Point point) {
    ObjectNode node = mapper.createObjectNode();
    node.put("x", point.x());
    node.put("y", point.y());
    return node;
  }

  private static ObjectNode encodeVector(ObjectMapper mapper, Vector2 vector) {
    ObjectNode node = mapper.createObjectNode();
    node.put("x", vector.x());
    node.put("y", vector.y());
    return node;
  }

  private static Vector2 validateVector(String key, Vector2 vector) {
    if (vector == null || !Float.isFinite(vector.x()) || !Float.isFinite(vector.y())) {
      throw invalid(key, "must contain finite x and y components");
    }
    return Vector2.of(vector.x(), vector.y());
  }

  private static Color validateColor(String key, Color color) {
    if (color == null
        || !validColorChannel(color.r)
        || !validColorChannel(color.g)
        || !validColorChannel(color.b)
        || !validColorChannel(color.a)) {
      throw invalid(key, "must contain finite RGBA channels between zero and one");
    }
    return color;
  }

  private static boolean validColorChannel(float value) {
    return Float.isFinite(value) && value >= 0 && value <= 1;
  }

  private static boolean isAsciiHex(String value) {
    for (int i = 0; i < value.length(); i++) {
      char digit = value.charAt(i);
      if (!((digit >= '0' && digit <= '9')
          || (digit >= 'a' && digit <= 'f')
          || (digit >= 'A' && digit <= 'F'))) {
        return false;
      }
    }
    return true;
  }
}
