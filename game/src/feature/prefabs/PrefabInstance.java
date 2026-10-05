package feature.prefabs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import tools.jackson.databind.JsonNode;

/**
 * Authored instance of a registered prefab.
 *
 * <p>The instance contains only stable level data. Runtime entity IDs and component state are not
 * stored here.
 *
 * @param name unique editor-facing name within the level
 * @param type stable prefab registry ID
 * @param properties serialized property values
 */
public record PrefabInstance(String name, String type, Map<String, JsonNode> properties) {

  /** Creates an immutable prefab instance with defensive copies of all JSON values. */
  public PrefabInstance {
    name = Objects.requireNonNull(name, "name");
    type = Objects.requireNonNull(type, "type");
    Objects.requireNonNull(properties, "properties");
    Map<String, JsonNode> copy = new LinkedHashMap<>();
    properties.forEach(
        (key, value) ->
            copy.put(
                Objects.requireNonNull(key, "property key"),
                Objects.requireNonNull(value, "property value").deepCopy()));
    properties = Collections.unmodifiableMap(copy);
  }

  /**
   * Creates a copy with one property replaced.
   *
   * @param key property key
   * @param value serialized value
   * @return updated immutable instance
   */
  public PrefabInstance withProperty(String key, JsonNode value) {
    Map<String, JsonNode> updated = new LinkedHashMap<>(properties);
    updated.put(key, value);
    return new PrefabInstance(name, type, updated);
  }

  /**
   * Creates a copy with one typed property value replaced.
   *
   * <p>Intended for building instances in code, e.g. {@code instance.with(WaterPrefab.REGION,
   * region).with(WaterPrefab.COLOR, color)}.
   *
   * @param property property descriptor of this instance's prefab type
   * @param value new typed value
   * @param <T> property value type
   * @return updated immutable instance
   */
  public <T> PrefabInstance with(PrefabProperty<T> property, T value) {
    return property.set(this, value);
  }

  /**
   * Reads a typed property value.
   *
   * @param property property descriptor of this instance's prefab type
   * @param <T> property value type
   * @return typed property value, or the property default if the value is missing
   */
  public <T> T get(PrefabProperty<T> property) {
    return property.get(this);
  }

  /**
   * Creates a copy with a different editor-facing name.
   *
   * @param newName new unique level-local name
   * @return updated immutable instance
   */
  public PrefabInstance withName(String newName) {
    return new PrefabInstance(newName, type, properties);
  }
}
