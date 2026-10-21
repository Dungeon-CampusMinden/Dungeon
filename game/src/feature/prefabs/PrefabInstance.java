package feature.prefabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
 * @param active whether the instance is spawned; inactive instances wait for {@link
 *     Prefab#ACTIVATE}. In level files this is whether the instance is active at level start.
 * @param inputs links to the output ports of other instances, by key of the receiving input port
 */
public record PrefabInstance(
    String name,
    String type,
    Map<String, JsonNode> properties,
    boolean active,
    Map<String, List<PrefabLink>> inputs) {

  /** Creates an immutable prefab instance with defensive copies of all values. */
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
    Objects.requireNonNull(inputs, "inputs");
    Map<String, List<PrefabLink>> inputCopy = new LinkedHashMap<>();
    inputs.forEach(
        (key, links) -> {
          Objects.requireNonNull(key, "input key");
          List<PrefabLink> linkCopy = List.copyOf(Objects.requireNonNull(links, "input links"));
          if (!linkCopy.isEmpty()) inputCopy.put(key, linkCopy);
        });
    inputs = Collections.unmodifiableMap(inputCopy);
  }

  /**
   * Creates an active, unlinked prefab instance.
   *
   * @param name unique editor-facing name within the level
   * @param type stable prefab registry ID
   * @param properties serialized property values
   */
  public PrefabInstance(String name, String type, Map<String, JsonNode> properties) {
    this(name, type, properties, true, Map.of());
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
    return new PrefabInstance(name, type, updated, active, inputs);
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
    return new PrefabInstance(newName, type, properties, active, inputs);
  }

  /**
   * Creates a copy that is active or inactive.
   *
   * @param newActive whether the instance is active
   * @return updated immutable instance
   */
  public PrefabInstance withActive(boolean newActive) {
    return new PrefabInstance(name, type, properties, newActive, inputs);
  }

  /**
   * Returns the links of one input port.
   *
   * @param port key of an action or artifact slot of this instance's prefab type
   * @return linked output ports, empty if the port is not linked
   */
  public List<PrefabLink> links(String port) {
    return inputs.getOrDefault(port, List.of());
  }

  /**
   * Returns the links of one input port.
   *
   * @param port action or artifact slot of this instance's prefab type
   * @return linked output ports, empty if the port is not linked
   */
  public List<PrefabLink> links(PrefabPort port) {
    return links(port.key());
  }

  /**
   * Creates a copy with the links of one input port replaced.
   *
   * @param port input port key
   * @param links new links, an empty list unlinks the port
   * @return updated immutable instance
   */
  public PrefabInstance withLinks(String port, List<PrefabLink> links) {
    Map<String, List<PrefabLink>> updated = new LinkedHashMap<>(inputs);
    if (links.isEmpty()) updated.remove(port);
    else updated.put(port, links);
    return new PrefabInstance(name, type, properties, active, updated);
  }

  /**
   * Creates a copy with one more link on an input port.
   *
   * @param port input port key
   * @param link new link
   * @return updated immutable instance
   */
  public PrefabInstance withLink(String port, PrefabLink link) {
    List<PrefabLink> updated = new ArrayList<>(links(port));
    updated.add(link);
    return withLinks(port, updated);
  }

  /**
   * Creates a copy with all input links replaced.
   *
   * @param newInputs new links by input port key
   * @return updated immutable instance
   */
  public PrefabInstance withInputs(Map<String, List<PrefabLink>> newInputs) {
    return new PrefabInstance(name, type, properties, active, newInputs);
  }
}
