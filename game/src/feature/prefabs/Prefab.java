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
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
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
   * Returns the properties currently shown for an instance, in descriptor order.
   *
   * <p>Properties can depend on other property values via {@link PrefabProperty#visibleWhen}.
   * Hidden properties keep their serialized values but are omitted from generated editor controls
   * and editor previews.
   *
   * @param instance normalized instance
   * @return visible property descriptors
   */
  public final List<PrefabProperty<?>> visibleProperties(PrefabInstance instance) {
    return properties.stream().filter(property -> property.isVisible(instance)).toList();
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
   * Resolves the latest data for this view.
   *
   * <p>This includes runtime changes made through {@link PrefabRuntime}. The result becomes empty
   * when the instance is deleted, removed at runtime, or replaced by another prefab type.
   *
   * @return the current normalized instance, if it is still active
   */
  public final Optional<PrefabInstance> currentInstance() {
    if (!isBound()) return Optional.empty();
    PrefabInstance latest = null;
    for (PrefabInstance instance : boundLevel.activePrefabs()) {
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
   * Respawns this instance from its current data, discarding the runtime state of its entities.
   *
   * <p>Must be called on the server. See {@link PrefabRuntime#reset}.
   *
   * @return true if the instance was active and has been reset
   */
  public final boolean reset() {
    requireBound();
    if (currentInstance().isEmpty()) return false;
    return PrefabRuntime.reset(boundLevel, boundName);
  }

  /**
   * Removes this instance and its entities for the rest of the game session. The level file is not
   * changed.
   *
   * <p>Must be called on the server. See {@link PrefabRuntime#remove}.
   *
   * @return true if the instance was active and has been removed
   */
  public final boolean remove() {
    requireBound();
    if (currentInstance().isEmpty()) return false;
    return PrefabRuntime.remove(boundLevel, boundName);
  }

  /**
   * Changes the data of this instance for the rest of the game session and respawns it. The level
   * file is not changed.
   *
   * <p>Must be called on the server. See {@link PrefabRuntime#update}.
   *
   * @param change creates the new data from the current one, e.g. {@code i -> i.with(PROPERTY,
   *     value)}
   * @throws IllegalArgumentException if the instance is not active or the new data is invalid
   */
  public final void update(UnaryOperator<PrefabInstance> change) {
    requireBound();
    if (currentInstance().isEmpty()) {
      throw new IllegalArgumentException("Prefab instance '" + boundName + "' is not active");
    }
    PrefabRuntime.update(boundLevel, boundName, change);
  }

  /**
   * Registers a listener for an event of this instance. Used by prefab types to offer typed
   * listener methods on their bound views.
   *
   * @param event event to listen to
   * @param listener called with the event payload
   * @param <T> payload type
   */
  protected final <T> void listen(PrefabEvent<T> event, Consumer<? super T> listener) {
    requireBound();
    PrefabRuntime.listen(boundLevel, boundName, event, listener);
  }

  /**
   * Notifies the listeners registered for an instance about an event.
   *
   * @param context creation context of the instance
   * @param instance instance raising the event
   * @param event raised event
   * @param payload event payload
   * @param <T> payload type
   */
  protected static <T> void fire(
      PrefabCreationContext context, PrefabInstance instance, PrefabEvent<T> event, T payload) {
    PrefabRuntime.fire(context.level(), instance.name(), event, payload);
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
