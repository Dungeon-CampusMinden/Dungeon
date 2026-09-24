package feature.prefabs;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Base class for every hard-coded level prefab definition. */
public abstract class Prefab {

  private static final ObjectMapper PROPERTY_MAPPER = new ObjectMapper();

  private final String type;
  private final String displayName;
  private final PrefabSide side;
  private final List<PrefabProperty<?>> properties;
  private final Map<String, PrefabProperty<?>> propertiesByKey;
  private ILevel boundLevel;
  private String boundName;

  /**
   * Creates a prefab definition.
   *
   * @param type stable serialized registry ID
   * @param displayName editor-facing prefab type name
   * @param side runtime creation side
   * @param properties ordered customizability settings
   */
  protected Prefab(
      String type, String displayName, PrefabSide side, List<PrefabProperty<?>> properties) {
    this(type, displayName, side, properties, null, null);
  }

  /**
   * Creates either a definition or a level-bound view of one authored instance.
   *
   * @param type stable serialized registry ID
   * @param displayName editor-facing prefab type name
   * @param side runtime creation side
   * @param properties ordered customizability settings
   * @param level owning level for a bound view, or {@code null} for a definition
   * @param instanceName authored instance name for a bound view, or {@code null} for a definition
   */
  protected Prefab(
      String type,
      String displayName,
      PrefabSide side,
      List<PrefabProperty<?>> properties,
      ILevel level,
      String instanceName) {
    this.type = requireText(type, "type");
    this.displayName = requireText(displayName, "displayName");
    this.side = Objects.requireNonNull(side, "side");
    if ((level == null) != (instanceName == null)) {
      throw new IllegalArgumentException("A bound prefab view requires both a level and a name");
    }
    this.boundLevel = level;
    this.boundName = instanceName;
    this.properties = List.copyOf(properties);
    Map<String, PrefabProperty<?>> byKey = new LinkedHashMap<>();
    for (PrefabProperty<?> property : this.properties) {
      if (byKey.putIfAbsent(property.key(), property) != null) {
        throw new IllegalArgumentException(
            "Duplicate property '" + property.key() + "' in prefab " + type);
      }
    }
    this.propertiesByKey = Map.copyOf(byKey);
  }

  /**
   * Returns the stable serialized registry ID.
   *
   * @return prefab type ID
   */
  public final String type() {
    return type;
  }

  /**
   * Returns the editor-facing prefab type name.
   *
   * @return display name
   */
  public final String displayName() {
    return displayName;
  }

  /**
   * Returns the runtime creation side.
   *
   * @return prefab side
   */
  public final PrefabSide side() {
    return side;
  }

  /**
   * Returns ordered descriptors for serialization and generated editor controls.
   *
   * @return property descriptors
   */
  public final List<PrefabProperty<?>> properties() {
    return properties;
  }

  /**
   * Returns whether this object is a per-instance view rather than a shared definition.
   *
   * @return true when bound to an authored instance
   */
  public final boolean isBound() {
    return boundLevel != null;
  }

  /**
   * Binds a newly-created registry view to one authored instance.
   *
   * @param level owning level
   * @param instanceName authored instance name
   * @return this bound view
   */
  final Prefab bind(ILevel level, String instanceName) {
    Objects.requireNonNull(level, "level");
    requireText(instanceName, "instanceName");
    if (isBound()) throw new IllegalStateException("Prefab view is already bound");
    boundLevel = level;
    boundName = instanceName;
    return this;
  }

  /**
   * Returns the authored instance name represented by this view.
   *
   * @return instance name
   * @throws IllegalStateException if this is an unbound prefab definition
   */
  public final String name() {
    requireBound();
    return boundName;
  }

  /**
   * Resolves the latest authored data for this view.
   *
   * <p>The result becomes empty when the instance is deleted or replaced by another prefab type.
   *
   * @return the current normalized authored instance, if it still exists
   */
  public final Optional<PrefabInstance> currentInstance() {
    if (!isBound()) return Optional.empty();
    PrefabInstance latest = null;
    for (PrefabInstance instance : boundLevel.prefabs()) {
      if (boundName.equals(instance.name())) latest = instance;
    }
    return latest == null || !type.equals(latest.type())
        ? Optional.empty()
        : Optional.of(normalize(latest));
  }

  /**
   * Returns the current serialized property values for this view.
   *
   * @return an immutable property map, or empty if the authored instance was deleted/replaced
   */
  public final Optional<Map<String, JsonNode>> currentProperties() {
    return currentInstance().map(PrefabInstance::properties);
  }

  /**
   * Returns entities currently spawned for this exact authored instance.
   *
   * <p>This is empty before spawning, on a runtime side where this prefab is not active, and after
   * despawn, deletion, replacement, or removal of the tracked entities. A subsequent respawn is
   * resolved dynamically and returns the new entities.
   *
   * @return live entities produced by this prefab instance
   */
  public final List<Entity> liveEntities() {
    if (!isBound()) return List.of();
    return PrefabSpawner.liveEntities(boundLevel, side, boundName, type);
  }

  /**
   * Builds a new instance populated with descriptor defaults.
   *
   * @param name unique level-local instance name
   * @return normalized prefab instance
   */
  public final PrefabInstance newInstance(String name) {
    return normalize(new PrefabInstance(name, type, Map.of()));
  }

  /**
   * Validates and normalizes an authored instance.
   *
   * @param instance instance to validate
   * @return instance containing every property in descriptor order
   */
  public final PrefabInstance normalize(PrefabInstance instance) {
    Objects.requireNonNull(instance, "instance");
    if (!type.equals(instance.type())) {
      throw new IllegalArgumentException(
          "Prefab type mismatch: expected '" + type + "', got '" + instance.type() + "'");
    }
    if (instance.name().isBlank()) {
      throw new IllegalArgumentException("Prefab instance name must not be blank");
    }
    for (String key : instance.properties().keySet()) {
      if (!propertiesByKey.containsKey(key)) {
        throw new IllegalArgumentException(
            "Unknown property '" + key + "' for prefab type '" + type + "'");
      }
    }

    Map<String, JsonNode> normalized = new LinkedHashMap<>();
    for (PrefabProperty<?> property : properties) {
      normalized.put(
          property.key(),
          property.normalize(PROPERTY_MAPPER, instance.properties().get(property.key())));
    }
    PrefabInstance result = new PrefabInstance(instance.name(), type, normalized);
    validate(result);
    return result;
  }

  /**
   * Translates all point properties for level-editor shift operations.
   *
   * @param instance source instance
   * @param translation world translation
   * @return translated normalized instance
   */
  public final PrefabInstance translate(PrefabInstance instance, Vector2 translation) {
    PrefabInstance translated = normalize(instance);
    for (PrefabProperty<?> property : properties) {
      if (property.type() == PrefabPropertyType.POINT) {
        @SuppressWarnings("unchecked")
        PrefabProperty<Point> pointProperty = (PrefabProperty<Point>) property;
        Point point = pointProperty.get(translated).translate(translation);
        translated =
            translated.withProperty(
                pointProperty.key(), pointProperty.encode(PROPERTY_MAPPER, point));
      } else if (property.type() == PrefabPropertyType.REGION) {
        @SuppressWarnings("unchecked")
        PrefabProperty<Region> regionProperty = (PrefabProperty<Region>) property;
        Region region = regionProperty.get(translated);
        Region shifted =
            new Region(
                region.bottomLeft().translate(translation),
                region.topRight().translate(translation));
        translated =
            translated.withProperty(
                regionProperty.key(), regionProperty.encode(PROPERTY_MAPPER, shifted));
      }
    }
    return normalize(translated);
  }

  /**
   * Performs cross-property or level-aware validation.
   *
   * @param instance normalized instance
   */
  protected void validate(PrefabInstance instance) {}

  /**
   * Creates all runtime entities produced by this prefab.
   *
   * @param context side-aware creation context
   * @param instance normalized instance
   * @return entities to add to the game
   */
  public abstract List<Entity> create(PrefabCreationContext context, PrefabInstance instance);

  /**
   * Renders prefab-specific editor feedback.
   *
   * @param level level currently being edited
   * @param instance normalized instance
   * @param feedback editor rendering abstraction
   * @param selected whether the instance is currently selected
   */
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {}

  /**
   * Cleans up side effects created while spawning an instance.
   *
   * @param context side-aware creation context
   * @param instance normalized instance
   */
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {}

  /**
   * Reads a typed property value from a normalized instance.
   *
   * @param instance normalized instance
   * @param property property descriptor
   * @param <T> property value type
   * @return typed property value
   */
  protected final <T> T value(PrefabInstance instance, PrefabProperty<T> property) {
    return property.get(instance);
  }

  private void requireBound() {
    if (!isBound()) {
      throw new IllegalStateException("This prefab is a definition, not an instance view");
    }
  }

  private static String requireText(String value, String field) {
    Objects.requireNonNull(value, field);
    if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    return value;
  }
}
