package feature.prefabs;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Base class for every hard-coded level prefab definition. */
public abstract class Prefab {

  /** Spawns an inactive instance. Every prefab has this action. */
  public static final PrefabAction ACTIVATE = PrefabAction.lifecycle("activate", "Activate");

  /** Despawns an active instance until it is activated again. Every prefab has this action. */
  public static final PrefabAction DEACTIVATE = PrefabAction.lifecycle("deactivate", "Deactivate");

  /** Respawns an active instance, discarding its runtime state. Every prefab has this action. */
  public static final PrefabAction RESET = PrefabAction.lifecycle("reset", "Reset");

  private static final List<PrefabPort> LIFECYCLE_PORTS = List.of(ACTIVATE, DEACTIVATE, RESET);

  private static final ObjectMapper PROPERTY_MAPPER = new ObjectMapper();

  private final String type;
  private final String displayName;
  private final PrefabSide side;
  private final List<PrefabProperty<?>> properties;
  private final Map<String, PrefabProperty<?>> propertiesByKey;
  private ILevel boundLevel;
  private String boundName;
  private List<PrefabPort> ports;
  private Map<String, PrefabPort> portsByKey;

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
   * Declares the events, actions, artifact outputs and artifact slots of this prefab type, in
   * editor order. The lifecycle actions every prefab has are added automatically.
   *
   * <p>Implementations must return the same ports on every call.
   *
   * @return declared ports
   */
  protected List<PrefabPort> declaredPorts() {
    return List.of();
  }

  /**
   * Returns all ports of this prefab type: the declared ports followed by the lifecycle actions.
   *
   * @return ports in editor order
   */
  public final List<PrefabPort> ports() {
    if (ports == null) {
      List<PrefabPort> all = new ArrayList<>(declaredPorts());
      all.addAll(LIFECYCLE_PORTS);
      Map<String, PrefabPort> byKey = new LinkedHashMap<>();
      for (PrefabPort port : all) {
        if (byKey.putIfAbsent(port.key(), port) != null) {
          throw new IllegalStateException("Duplicate port '" + port.key() + "' in prefab " + type);
        }
      }
      portsByKey = Map.copyOf(byKey);
      ports = List.copyOf(all);
    }
    return ports;
  }

  /**
   * Looks up a port of this prefab type.
   *
   * @param key port key
   * @return the port, if this type has one with the key
   */
  public final Optional<PrefabPort> port(String key) {
    ports();
    return Optional.ofNullable(portsByKey.get(key));
  }

  /**
   * Returns the ports of one kind, in editor order.
   *
   * @param kind port kind
   * @return matching ports
   */
  public final List<PrefabPort> ports(PrefabPort.Kind kind) {
    return ports().stream().filter(port -> port.kind() == kind).toList();
  }

  /**
   * Returns the ports an instance offers, see {@link #isPortAvailable}.
   *
   * @param instance instance of this prefab type
   * @return available ports in editor order
   */
  public final List<PrefabPort> ports(PrefabInstance instance) {
    return ports().stream().filter(port -> isPortAvailable(instance, port)).toList();
  }

  /**
   * Returns the ports of one kind an instance offers, see {@link #isPortAvailable}.
   *
   * @param instance instance of this prefab type
   * @param kind port kind
   * @return available ports of the kind, in editor order
   */
  public final List<PrefabPort> ports(PrefabInstance instance, PrefabPort.Kind kind) {
    return ports(kind).stream().filter(port -> isPortAvailable(instance, port)).toList();
  }

  /**
   * Looks up a port an instance offers, see {@link #isPortAvailable}.
   *
   * @param instance instance of this prefab type
   * @param key port key
   * @return the port, if this type has one with the key and the instance offers it
   */
  public final Optional<PrefabPort> port(PrefabInstance instance, String key) {
    return port(key).filter(port -> isPortAvailable(instance, port));
  }

  /**
   * Decides whether an instance offers one of the ports of this type, e.g. one artifact output per
   * entry of an authored list.
   *
   * <p>Ports that are not available cannot be linked. Links stored on an instance for its
   * unavailable inputs are dropped by {@link #normalize}, links of other instances to its
   * unavailable outputs are broken, see {@link PrefabWiring#withoutLinksToUnavailableOutputs}. The
   * result may only depend on the property values of the instance.
   *
   * @param instance instance of this prefab type, possibly not normalized
   * @param port one of {@link #ports()}
   * @return true if the instance offers the port; true for every port by default
   */
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    return true;
  }

  /**
   * Returns the editor-facing name of a port of an instance, e.g. including the authored text of
   * the list entry the port belongs to.
   *
   * @param instance normalized instance of this prefab type
   * @param port one of {@link #ports()}
   * @return port name; {@link PrefabPort#displayName()} by default
   */
  public String portDisplayName(PrefabInstance instance, PrefabPort port) {
    return port.displayName();
  }

  /**
   * Returns the artifact slot replacing a property while it is linked.
   *
   * @param property property of this prefab type
   * @return the slot, if the property can be replaced by an artifact
   */
  public final Optional<ArtifactSlot<?>> slotFor(PrefabProperty<?> property) {
    for (PrefabPort port : ports()) {
      if (port instanceof ArtifactSlot<?> slot
          && slot.property().filter(p -> p == property).isPresent()) {
        return Optional.of(slot);
      }
    }
    return Optional.empty();
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
   * Runs an action of this instance as if a linked event was raised.
   *
   * <p>Must be called on the server.
   *
   * @param action action of this prefab type, including the lifecycle actions
   * @return true if the instance exists and the action ran
   */
  public final boolean trigger(PrefabAction action) {
    requireBound();
    return PrefabRuntime.runAction(boundLevel, boundName, action.key(), Optional.empty());
  }

  /**
   * Returns the value this instance currently publishes on an artifact output.
   *
   * @param output artifact output of this prefab type
   * @param <T> value type
   * @return the published value, if any
   */
  public final <T> Optional<T> artifact(ArtifactOutput<T> output) {
    requireBound();
    return PrefabRuntime.artifact(boundLevel, boundName, output);
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
   * Publishes the value of an artifact output for an instance, replacing its previous value.
   *
   * <p>Usually called while creating the instance, or when the value becomes available later.
   * Values are dropped when the instance is despawned.
   *
   * @param context creation context of the instance
   * @param instance instance producing the value
   * @param output artifact output of this prefab type
   * @param value produced value
   * @param <T> value type
   */
  protected static <T> void publish(
      PrefabCreationContext context, PrefabInstance instance, ArtifactOutput<T> output, T value) {
    PrefabRuntime.publish(context.level(), instance.name(), output, value);
  }

  /**
   * Returns the values linked to an artifact slot of an instance.
   *
   * <p>The returned input resolves the current values on every call, so it can be kept by the
   * spawned entities.
   *
   * @param context creation context of the instance
   * @param instance consuming instance
   * @param slot artifact slot of this prefab type
   * @param <T> value type
   * @return input reading the linked values
   */
  protected static <T> ArtifactInput<T> input(
      PrefabCreationContext context, PrefabInstance instance, ArtifactSlot<T> slot) {
    return new ArtifactInput<>(context.level(), instance.name(), slot, instance.links(slot));
  }

  /**
   * Returns a property value that can be replaced by a linked artifact.
   *
   * <p>While the slot replacing the property is linked, the current artifact value is returned, or
   * empty if it is not available. Otherwise the authored property value is returned.
   *
   * @param context creation context of the instance
   * @param instance consuming instance
   * @param property property of this prefab type that has a slot, see {@link
   *     ArtifactSlot#forProperty}
   * @param <T> value type
   * @return supplier of the current value
   */
  protected final <T> Supplier<Optional<T>> linkableValue(
      PrefabCreationContext context, PrefabInstance instance, PrefabProperty<T> property) {
    @SuppressWarnings("unchecked")
    ArtifactSlot<T> slot =
        (ArtifactSlot<T>)
            slotFor(property)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Property '" + property.key() + "' has no artifact slot"));
    if (instance.links(slot).isEmpty()) {
      T value = property.get(instance);
      return () -> Optional.of(value);
    }
    ArtifactInput<T> input = input(context, instance, slot);
    return input::value;
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
      if (!propertiesByKey.containsKey(key) && !isLegacyProperty(key)) {
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
    PrefabInstance withProperties =
        new PrefabInstance(instance.name(), type, normalized, instance.active(), instance.inputs());
    PrefabInstance result =
        new PrefabInstance(
            instance.name(), type, normalized, instance.active(), normalizeInputs(withProperties));
    validate(result);
    return result;
  }

  private Map<String, List<PrefabLink>> normalizeInputs(PrefabInstance instance) {
    for (String key : instance.inputs().keySet()) {
      Optional<PrefabPort> port = port(key);
      if (port.isEmpty()) {
        throw new IllegalArgumentException(
            "Unknown input port '" + key + "' for prefab type '" + type + "'");
      }
      if (!port.get().kind().isInput()) {
        throw new IllegalArgumentException(
            "Port '" + key + "' of prefab type '" + type + "' cannot receive links");
      }
    }
    Map<String, List<PrefabLink>> result = new LinkedHashMap<>();
    for (PrefabPort port : ports()) {
      List<PrefabLink> links = instance.links(port.key());
      if (!port.kind().isInput() || links.isEmpty() || !isPortAvailable(instance, port)) continue;
      boolean aliased = port instanceof ArtifactSlot<?> slot && slot.multiple();
      List<PrefabLink> normalized = new ArrayList<>();
      Set<String> aliases = new HashSet<>();
      for (PrefabLink link : links) {
        if (normalized.stream().anyMatch(existing -> existing.sameOutput(link))) continue;
        if (port.kind() == PrefabPort.Kind.ARTIFACT_SLOT && link.source().equals(instance.name())) {
          throw new IllegalArgumentException(
              "Artifact slot '" + port.key() + "' cannot be linked to its own instance");
        }
        if (!aliased) {
          normalized.add(link.withAlias(null));
          continue;
        }
        String alias = link.alias();
        if (alias == null) {
          alias = PrefabWiring.uniqueAlias(PrefabWiring.defaultAlias(link.source()), aliases);
        } else if (aliases.contains(alias)) {
          throw new IllegalArgumentException(
              "Duplicate alias '" + alias + "' in artifact slot '" + port.key() + "'");
        }
        aliases.add(alias);
        normalized.add(link.withAlias(alias));
      }
      if (port instanceof ArtifactSlot<?> slot && !slot.multiple() && normalized.size() > 1) {
        throw new IllegalArgumentException(
            "Artifact slot '" + port.key() + "' takes only one value");
      }
      result.put(port.key(), normalized);
    }
    return result;
  }

  /**
   * Identifies retired properties that are accepted when loading older instances but discarded
   * during normalization. Other unknown properties remain invalid.
   *
   * @param key serialized property key
   * @return whether this retired property can be discarded
   */
  protected boolean isLegacyProperty(String key) {
    return false;
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
      } else if (property.type() == PrefabPropertyType.IMAGE_PUZZLE_SETTINGS) {
        @SuppressWarnings("unchecked")
        PrefabProperty<ImagePuzzleSettings> settingsProperty =
            (PrefabProperty<ImagePuzzleSettings>) property;
        ImagePuzzleSettings settings = settingsProperty.get(translated);
        translated =
            translated.withProperty(
                settingsProperty.key(),
                settingsProperty.encode(PROPERTY_MAPPER, settings.translated(translation)));
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
   * Returns a short description of an instance, e.g. the sound it plays. The editor shows it next
   * to the selected instance if the prefab has no world position (no point or region property) and
   * is therefore drawn next to the prefabs it is linked to.
   *
   * @param instance normalized instance
   * @return summary, if the prefab has one
   */
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.empty();
  }

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
