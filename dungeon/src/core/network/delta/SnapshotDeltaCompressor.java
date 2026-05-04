package core.network.delta;

import contrib.item.Item;
import core.level.utils.Coordinate;
import core.network.codec.CommonProtoConverters;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.DoorTileState;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.EntityStateField;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.Point;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Builds and applies field-level deltas between full snapshot baselines and current snapshots. */
public final class SnapshotDeltaCompressor {

  private SnapshotDeltaCompressor() {}

  /**
   * Creates a delta from a full baseline snapshot to the current full snapshot.
   *
   * @param baseline full snapshot used as the delta base
   * @param current current full snapshot
   * @return a delta snapshot if anything changed, otherwise an empty Optional
   */
  public static Optional<DeltaSnapshotMessage> compress(
      SnapshotMessage baseline, SnapshotMessage current) {
    Objects.requireNonNull(baseline, "baseline");
    return compress(baseline, current, entitiesById(baseline).keySet());
  }

  /**
   * Creates a delta from a full baseline snapshot to the current full snapshot.
   *
   * <p>The known entity IDs include all entities sent to the client since the current full
   * baseline. This lets the delta report removals for entities that were created after the baseline
   * and removed before the next full snapshot.
   *
   * @param baseline full snapshot used as the delta base
   * @param current current full snapshot
   * @param knownEntityIds entity IDs sent to the client since the full baseline
   * @return a delta snapshot if anything changed, otherwise an empty Optional
   */
  public static Optional<DeltaSnapshotMessage> compress(
      SnapshotMessage baseline, SnapshotMessage current, Collection<Integer> knownEntityIds) {
    Objects.requireNonNull(baseline, "baseline");
    Objects.requireNonNull(current, "current");
    Objects.requireNonNull(knownEntityIds, "knownEntityIds");

    Map<Integer, EntityState> baselineEntities = entitiesById(baseline);
    Map<Integer, EntityState> currentEntities = entitiesById(current);

    List<EntityDelta> entityDeltas = new ArrayList<>();
    for (EntityState currentState : current.entities()) {
      EntityState baselineState = baselineEntities.get(currentState.entityId());
      if (baselineState == null) {
        entityDeltas.add(new EntityDelta(currentState.entityId(), copyOf(currentState), Set.of()));
        continue;
      }
      createEntityDelta(baselineState, currentState).ifPresent(entityDeltas::add);
    }

    Set<Integer> removableEntityIds = new LinkedHashSet<>(baselineEntities.keySet());
    removableEntityIds.addAll(knownEntityIds);
    List<Integer> removedEntityIds =
        removableEntityIds.stream().filter(id -> !currentEntities.containsKey(id)).toList();
    LevelState levelStateDelta = levelStateDelta(baseline.levelState(), current.levelState());

    DeltaSnapshotMessage delta =
        new DeltaSnapshotMessage(
            baseline.serverTick(),
            current.serverTick(),
            entityDeltas,
            removedEntityIds,
            levelStateDelta);
    return delta.hasChanges() ? Optional.of(delta) : Optional.empty();
  }

  /**
   * Materializes the entity changes in a delta against its full snapshot baseline.
   *
   * <p>The returned snapshot contains only changed entities and the level-state delta. It is meant
   * to be applied through the normal snapshot application path after removals are handled.
   *
   * @param baseline full snapshot used as the delta base
   * @param delta delta snapshot
   * @return snapshot containing merged changed entities
   */
  public static SnapshotMessage materializeChangedSnapshot(
      SnapshotMessage baseline, DeltaSnapshotMessage delta) {
    Objects.requireNonNull(baseline, "baseline");
    Objects.requireNonNull(delta, "delta");
    if (baseline.serverTick() != delta.baseTick()) {
      throw new IllegalArgumentException(
          "Delta base tick "
              + delta.baseTick()
              + " does not match baseline tick "
              + baseline.serverTick()
              + ".");
    }

    Map<Integer, EntityState> baselineEntities = entitiesById(baseline);
    List<EntityState> changedEntities = new ArrayList<>();
    for (EntityDelta entityDelta : delta.entityDeltas()) {
      EntityState baselineState = baselineEntities.get(entityDelta.entityId());
      changedEntities.add(
          baselineState == null
              ? copyOf(entityDelta.changedState())
              : mergeEntityState(baselineState, entityDelta));
    }

    return new SnapshotMessage(
        delta.serverTick(),
        changedEntities,
        delta.levelStateDeltaOptional().orElseGet(() -> new LevelState(Set.of())));
  }

  private static Optional<EntityDelta> createEntityDelta(
      EntityState baseline, EntityState current) {
    EntityState.Builder builder = EntityState.builder().entityId(current.entityId());
    EnumSet<EntityStateField> clearedFields = EnumSet.noneOf(EntityStateField.class);
    boolean[] hasChangedFields = {false};

    diffOptional(
        baseline.entityName(),
        current.entityName(),
        builder::entityName,
        clearedFields,
        EntityStateField.ENTITY_NAME,
        hasChangedFields);
    diffOptional(
        baseline.position(),
        current.position(),
        builder::position,
        clearedFields,
        EntityStateField.POSITION,
        hasChangedFields);
    diffOptional(
        baseline.viewDirection(),
        current.viewDirection(),
        builder::viewDirection,
        clearedFields,
        EntityStateField.VIEW_DIRECTION,
        hasChangedFields);
    diffOptional(
        baseline.rotation(),
        current.rotation(),
        builder::rotation,
        clearedFields,
        EntityStateField.ROTATION,
        hasChangedFields);
    diffOptional(
        baseline.scale(),
        current.scale(),
        builder::scale,
        clearedFields,
        EntityStateField.SCALE,
        hasChangedFields,
        SnapshotDeltaCompressor::vectorEquals);
    diffOptional(
        baseline.currentHealth(),
        current.currentHealth(),
        builder::currentHealth,
        clearedFields,
        EntityStateField.CURRENT_HEALTH,
        hasChangedFields);
    diffOptional(
        baseline.maxHealth(),
        current.maxHealth(),
        builder::maxHealth,
        clearedFields,
        EntityStateField.MAX_HEALTH,
        hasChangedFields);
    diffOptional(
        baseline.currentMana(),
        current.currentMana(),
        builder::currentMana,
        clearedFields,
        EntityStateField.CURRENT_MANA,
        hasChangedFields);
    diffOptional(
        baseline.maxMana(),
        current.maxMana(),
        builder::maxMana,
        clearedFields,
        EntityStateField.MAX_MANA,
        hasChangedFields);
    diffOptional(
        baseline.stateName(),
        current.stateName(),
        builder::stateName,
        clearedFields,
        EntityStateField.STATE_NAME,
        hasChangedFields);
    diffOptional(
        baseline.tintColor(),
        current.tintColor(),
        builder::tintColor,
        clearedFields,
        EntityStateField.TINT_COLOR,
        hasChangedFields);
    diffOptional(
        baseline.inventory(),
        current.inventory(),
        items -> builder.inventory(items.clone()),
        clearedFields,
        EntityStateField.INVENTORY,
        hasChangedFields,
        SnapshotDeltaCompressor::inventoryEquals);
    diffOptional(
        baseline.metadata(),
        current.metadata(),
        builder::metadata,
        clearedFields,
        EntityStateField.METADATA,
        hasChangedFields);

    if (!hasChangedFields[0] && clearedFields.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new EntityDelta(current.entityId(), builder.build(), clearedFields));
  }

  private static <T> void diffOptional(
      Optional<T> baseline,
      Optional<T> current,
      java.util.function.Consumer<T> setter,
      Set<EntityStateField> clearedFields,
      EntityStateField field,
      boolean[] hasChangedFields) {
    diffOptional(
        baseline, current, setter, clearedFields, field, hasChangedFields, Objects::equals);
  }

  private static <T> void diffOptional(
      Optional<T> baseline,
      Optional<T> current,
      java.util.function.Consumer<T> setter,
      Set<EntityStateField> clearedFields,
      EntityStateField field,
      boolean[] hasChangedFields,
      java.util.function.BiPredicate<T, T> equality) {
    if (current.isPresent()) {
      T currentValue = current.orElseThrow();
      if (baseline.isEmpty() || !equality.test(baseline.orElseThrow(), currentValue)) {
        setter.accept(currentValue);
        hasChangedFields[0] = true;
      }
    } else if (baseline.isPresent()) {
      clearedFields.add(field);
    }
  }

  private static EntityState mergeEntityState(EntityState baseline, EntityDelta delta) {
    MutableEntityState mutable = MutableEntityState.from(baseline);
    delta.clearedFields().forEach(mutable::clear);
    mutable.apply(delta.changedState());
    return mutable.build();
  }

  private static EntityState copyOf(EntityState state) {
    return MutableEntityState.from(state).build();
  }

  private static Map<Integer, EntityState> entitiesById(SnapshotMessage snapshot) {
    Map<Integer, EntityState> result = new LinkedHashMap<>();
    for (EntityState entityState : snapshot.entities()) {
      result.put(entityState.entityId(), entityState);
    }
    return result;
  }

  private static LevelState levelStateDelta(LevelState baseline, LevelState current) {
    Map<Coordinate, DoorTileState> baselineDoors = doorStatesByCoordinate(baseline);
    Set<DoorTileState> changedDoorStates = new LinkedHashSet<>();
    for (DoorTileState currentDoor : current.doorStates()) {
      DoorTileState baselineDoor = baselineDoors.get(currentDoor.coordinate());
      if (baselineDoor == null || baselineDoor.open() != currentDoor.open()) {
        changedDoorStates.add(currentDoor);
      }
    }
    return changedDoorStates.isEmpty() ? null : new LevelState(changedDoorStates);
  }

  private static Map<Coordinate, DoorTileState> doorStatesByCoordinate(LevelState levelState) {
    Map<Coordinate, DoorTileState> result = new LinkedHashMap<>();
    for (DoorTileState doorState : levelState.doorStates()) {
      result.put(doorState.coordinate(), doorState);
    }
    return result;
  }

  private static boolean vectorEquals(Vector2 left, Vector2 right) {
    return Float.compare(left.x(), right.x()) == 0 && Float.compare(left.y(), right.y()) == 0;
  }

  private static boolean inventoryEquals(Item[] left, Item[] right) {
    if (left == right) {
      return true;
    }
    if (left == null || right == null || left.length != right.length) {
      return false;
    }
    for (int i = 0; i < left.length; i++) {
      Item leftItem = left[i];
      Item rightItem = right[i];
      if (leftItem == rightItem) {
        continue;
      }
      if (leftItem == null || rightItem == null) {
        return false;
      }
      if (!CommonProtoConverters.toProto(leftItem)
          .equals(CommonProtoConverters.toProto(rightItem))) {
        return false;
      }
    }
    return true;
  }

  private static final class MutableEntityState {
    private int entityId;
    private String entityName;
    private Point position;
    private String viewDirection;
    private Float rotation;
    private Vector2 scale;
    private Integer currentHealth;
    private Integer maxHealth;
    private Float currentMana;
    private Float maxMana;
    private String stateName;
    private Integer tintColor;
    private Item[] inventory;
    private Map<String, String> metadata;

    static MutableEntityState from(EntityState state) {
      MutableEntityState mutable = new MutableEntityState();
      mutable.entityId = state.entityId();
      mutable.apply(state);
      return mutable;
    }

    void apply(EntityState state) {
      state.entityName().ifPresent(value -> entityName = value);
      state.position().ifPresent(value -> position = value);
      state.viewDirection().ifPresent(value -> viewDirection = value);
      state.rotation().ifPresent(value -> rotation = value);
      state.scale().ifPresent(value -> scale = value);
      state.currentHealth().ifPresent(value -> currentHealth = value);
      state.maxHealth().ifPresent(value -> maxHealth = value);
      state.currentMana().ifPresent(value -> currentMana = value);
      state.maxMana().ifPresent(value -> maxMana = value);
      state.stateName().ifPresent(value -> stateName = value);
      state.tintColor().ifPresent(value -> tintColor = value);
      state.inventory().ifPresent(items -> inventory = items.clone());
      state.metadata().ifPresent(value -> metadata = Map.copyOf(value));
    }

    void clear(EntityStateField field) {
      switch (field) {
        case ENTITY_NAME -> entityName = null;
        case POSITION -> position = null;
        case VIEW_DIRECTION -> viewDirection = null;
        case ROTATION -> rotation = null;
        case SCALE -> scale = null;
        case CURRENT_HEALTH -> currentHealth = null;
        case MAX_HEALTH -> maxHealth = null;
        case CURRENT_MANA -> currentMana = null;
        case MAX_MANA -> maxMana = null;
        case STATE_NAME -> stateName = null;
        case TINT_COLOR -> tintColor = null;
        case INVENTORY -> inventory = null;
        case METADATA -> metadata = null;
      }
    }

    EntityState build() {
      EntityState.Builder builder = EntityState.builder().entityId(entityId);
      setIfNotNull(entityName, builder::entityName);
      setIfNotNull(position, builder::position);
      setIfNotNull(viewDirection, builder::viewDirection);
      setIfNotNull(rotation, builder::rotation);
      setIfNotNull(scale, builder::scale);
      setIfNotNull(currentHealth, builder::currentHealth);
      setIfNotNull(maxHealth, builder::maxHealth);
      setIfNotNull(currentMana, builder::currentMana);
      setIfNotNull(maxMana, builder::maxMana);
      setIfNotNull(stateName, builder::stateName);
      setIfNotNull(tintColor, builder::tintColor);
      setIfNotNull(inventory, items -> builder.inventory(items.clone()));
      setIfNotNull(metadata, builder::metadata);
      return builder.build();
    }

    private static <T> void setIfNotNull(T value, java.util.function.Consumer<T> setter) {
      if (value != null) {
        setter.accept(value);
      }
    }
  }
}
