package feature.prefabs;

import engine.Entity;
import engine.Game;
import engine.level.elements.ILevel;
import feature.components.LeverComponent;
import feature.components.PressurePlateComponent;
import feature.components.ShowImageComponent;
import feature.level.visibility.LevelHideComponent;
import feature.level.visibility.LevelHideSystem;
import feature.prefabs.types.DesignLabelRegionPrefab;
import feature.systems.LeverSystem;
import feature.systems.PressurePlateSystem;
import feature.systems.ShowImageSystem;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Creates authored prefab instances for one runtime side. */
public final class PrefabSpawner {

  private static final Set<Entity> CLIENT_ENTITIES = new LinkedHashSet<>();

  private record TrackedPrefab(
      ILevel level, PrefabInstance source, Prefab prefab, List<Entity> entities) {}

  private static final Map<ILevel, Map<PrefabSide, Map<String, TrackedPrefab>>> TRACKED =
      new IdentityHashMap<>();

  private PrefabSpawner() {}

  /**
   * Creates and adds all prefabs assigned to the requested side.
   *
   * @param level owning level
   * @param side side to instantiate
   */
  public static void spawn(ILevel level, PrefabSide side) {
    for (PrefabInstance source : level.prefabs()) {
      Prefab prefab = PrefabRegistry.require(source.type());
      if (prefab.side() != side) continue;
      spawnInstance(level, source, side);
    }
  }

  /**
   * Spawns one instance and records its exact entity identity for later cleanup.
   *
   * @param level owning level
   * @param source authored instance
   * @param side runtime side
   */
  public static void spawnInstance(ILevel level, PrefabInstance source, PrefabSide side) {
    Prefab prefab = PrefabRegistry.require(source.type());
    if (prefab.side() != side) return;
    PrefabInstance instance = prefab.normalize(source);
    despawnInstance(level, instance, side);
    PrefabCreationContext context = new PrefabCreationContext(level, side);
    List<Entity> added = new ArrayList<>();
    List<Entity> created;
    try {
      created = List.copyOf(prefab.create(context, instance));
    } catch (RuntimeException e) {
      throw creationFailureWithCleanup(instance, prefab, context, added, null, e);
    }
    TrackedPrefab tracked = null;
    try {
      ensureRequiredSystems(created);
      for (Entity entity : created) {
        added.add(entity);
        Game.add(entity);
      }
      tracked = new TrackedPrefab(level, instance, prefab, List.copyOf(added));
      TRACKED
          .computeIfAbsent(level, ignored -> new EnumMap<>(PrefabSide.class))
          .computeIfAbsent(side, ignored -> new LinkedHashMap<>())
          .put(instance.name(), tracked);
      if (side == PrefabSide.CLIENT) CLIENT_ENTITIES.addAll(added);
      refreshDesignLabelRegions(level, side);
    } catch (RuntimeException e) {
      throw creationFailureWithCleanup(instance, prefab, context, added, tracked, e);
    }
  }

  /**
   * Returns whether a normalized authored instance currently has an active tracked spawn.
   *
   * <p>Entity-less client prefabs are considered active while tracked. This is used by region
   * overlays, which intentionally create no entities.
   *
   * @param level owning level
   * @param side runtime side
   * @param name authored instance name
   * @param type stable prefab type
   * @return whether this exact authored instance is spawned
   */
  public static boolean isActive(ILevel level, PrefabSide side, String name, String type) {
    Map<PrefabSide, Map<String, TrackedPrefab>> bySide = TRACKED.get(level);
    if (bySide == null || bySide.get(side) == null) return false;
    TrackedPrefab tracked = bySide.get(side).get(name);
    if (tracked == null || !type.equals(tracked.source().type())) return false;
    PrefabInstance authored =
        level.prefabs().stream()
            .filter(instance -> name.equals(instance.name()))
            .reduce((first, second) -> second)
            .orElse(null);
    if (authored == null || !type.equals(authored.type())) return false;
    try {
      return tracked.source().equals(PrefabRegistry.require(type).normalize(authored));
    } catch (IllegalArgumentException invalidAuthoredRecord) {
      return false;
    }
  }

  /**
   * Reapplies client-side design-region overrides over the level's persistent base design.
   *
   * @param level owning level
   */
  public static void refreshDesignLabelRegions(ILevel level) {
    DesignLabelRegionPrefab.refresh(level);
  }

  /**
   * Resolves the currently tracked entities for a bound prefab view.
   *
   * <p>Both the authored record and the entity objects are checked by identity/current value so
   * stale views do not expose entities from a deleted, edited, replaced, or despawned instance.
   *
   * @param level owning level
   * @param side runtime side
   * @param name authored instance name
   * @param type stable prefab type
   * @return present entities from the current spawn, in creation order
   */
  public static List<Entity> liveEntities(ILevel level, PrefabSide side, String name, String type) {
    PrefabInstance authored = null;
    for (PrefabInstance candidate : level.prefabs()) {
      if (name.equals(candidate.name())) authored = candidate;
    }
    if (authored == null || !type.equals(authored.type())) return List.of();

    Map<PrefabSide, Map<String, TrackedPrefab>> bySide = TRACKED.get(level);
    if (bySide == null || bySide.get(side) == null) return List.of();
    TrackedPrefab tracked = bySide.get(side).get(name);
    if (tracked == null || !type.equals(tracked.source().type())) return List.of();

    PrefabInstance normalized;
    try {
      normalized = PrefabRegistry.require(type).normalize(authored);
    } catch (IllegalArgumentException invalidAuthoredRecord) {
      return List.of();
    }
    if (!tracked.source().equals(normalized)) return List.of();
    return tracked.entities().stream().filter(PrefabSpawner::isPresent).toList();
  }

  /**
   * Removes one tracked instance and invokes its side-effect cleanup hook.
   *
   * @param level owning level
   * @param instance authored instance
   * @param side runtime side
   */
  public static void despawnInstance(ILevel level, PrefabInstance instance, PrefabSide side) {
    Map<PrefabSide, Map<String, TrackedPrefab>> bySide = TRACKED.get(level);
    if (bySide == null || bySide.get(side) == null) return;
    TrackedPrefab tracked = bySide.get(side).remove(instance.name());
    try {
      if (tracked != null) removeTracked(tracked, side);
    } finally {
      discardEmpty(level, bySide, side);
      refreshDesignLabelRegions(level, side);
    }
  }

  /**
   * Clears every tracked instance for a level and side.
   *
   * @param level owning level
   * @param side runtime side
   */
  public static void clear(ILevel level, PrefabSide side) {
    Map<PrefabSide, Map<String, TrackedPrefab>> bySide = TRACKED.get(level);
    if (bySide == null) return;
    Map<String, TrackedPrefab> tracked = bySide.remove(side);
    if (tracked == null) return;
    if (bySide.isEmpty()) TRACKED.remove(level);
    RuntimeException failure = null;
    for (TrackedPrefab record : new ArrayList<>(tracked.values())) {
      try {
        removeTracked(record, side);
      } catch (RuntimeException exception) {
        if (failure == null) failure = exception;
        else failure.addSuppressed(exception);
      }
    }
    refreshDesignLabelRegions(level, side);
    if (failure != null) throw failure;
  }

  /**
   * Removes entities previously created for the requested side.
   *
   * <p>Only client-side prefabs require explicit tracking because authoritative level cleanup
   * already removes server entities.
   *
   * @param side side to clear
   */
  public static void clear(PrefabSide side) {
    for (ILevel level : new ArrayList<>(TRACKED.keySet())) clear(level, side);
    if (side == PrefabSide.CLIENT) {
      CLIENT_ENTITIES.stream().filter(PrefabSpawner::isPresent).toList().forEach(Game::remove);
      CLIENT_ENTITIES.clear();
    }
  }

  private static void removeTracked(TrackedPrefab tracked, PrefabSide side) {
    try {
      tracked
          .prefab()
          .onDespawn(
              new PrefabCreationContext(tracked.level(), side),
              tracked.prefab().normalize(tracked.source()));
    } finally {
      tracked.entities().stream().filter(PrefabSpawner::isPresent).forEach(Game::remove);
      if (side == PrefabSide.CLIENT) CLIENT_ENTITIES.removeAll(tracked.entities());
    }
  }

  private static void refreshDesignLabelRegions(ILevel level, PrefabSide side) {
    if (side == PrefabSide.CLIENT) refreshDesignLabelRegions(level);
  }

  private static IllegalStateException creationFailureWithCleanup(
      PrefabInstance instance,
      Prefab prefab,
      PrefabCreationContext context,
      List<Entity> added,
      TrackedPrefab tracked,
      RuntimeException cause) {
    IllegalStateException failure = creationFailure(instance, cause);
    if (tracked != null) removeTrackedRecord(tracked);
    try {
      prefab.onDespawn(context, instance);
    } catch (RuntimeException cleanupFailure) {
      failure.addSuppressed(cleanupFailure);
    }
    try {
      removeAdded(added);
    } catch (RuntimeException cleanupFailure) {
      failure.addSuppressed(cleanupFailure);
    }
    try {
      refreshDesignLabelRegions(context.level(), context.side());
    } catch (RuntimeException cleanupFailure) {
      failure.addSuppressed(cleanupFailure);
    }
    return failure;
  }

  private static void removeTrackedRecord(TrackedPrefab tracked) {
    Map<PrefabSide, Map<String, TrackedPrefab>> bySide = TRACKED.get(tracked.level());
    if (bySide == null) return;
    Map<String, TrackedPrefab> byName = bySide.get(tracked.prefab().side());
    if (byName == null) return;
    if (byName.get(tracked.source().name()) == tracked) byName.remove(tracked.source().name());
    discardEmpty(tracked.level(), bySide, tracked.prefab().side());
  }

  private static void removeAdded(List<Entity> added) {
    RuntimeException failure = null;
    for (Entity entity : added) {
      try {
        if (isPresent(entity)) Game.remove(entity);
      } catch (RuntimeException exception) {
        if (failure == null) failure = exception;
        else failure.addSuppressed(exception);
      }
    }
    CLIENT_ENTITIES.removeAll(added);
    if (failure != null) throw failure;
  }

  private static void discardEmpty(
      ILevel level, Map<PrefabSide, Map<String, TrackedPrefab>> bySide, PrefabSide side) {
    if (bySide.get(side) != null && bySide.get(side).isEmpty()) bySide.remove(side);
    if (bySide.isEmpty()) TRACKED.remove(level);
  }

  private static boolean isPresent(Entity entity) {
    return Game.findEntityById(entity.id()).filter(current -> current == entity).isPresent();
  }

  private static void ensureRequiredSystems(List<Entity> entities) {
    if (entities.stream().anyMatch(entity -> entity.isPresent(LeverComponent.class))
        && !Game.systems().containsKey(LeverSystem.class)) {
      Game.add(new LeverSystem());
    }
    if (entities.stream().anyMatch(entity -> entity.isPresent(PressurePlateComponent.class))
        && !Game.systems().containsKey(PressurePlateSystem.class)) {
      Game.add(new PressurePlateSystem());
    }
    if (entities.stream().anyMatch(entity -> entity.isPresent(ShowImageComponent.class))
        && !Game.systems().containsKey(ShowImageSystem.class)) {
      Game.add(new ShowImageSystem());
    }
    if (entities.stream().anyMatch(entity -> entity.isPresent(LevelHideComponent.class))
        && !Game.systems().containsKey(LevelHideSystem.class)) {
      Game.add(new LevelHideSystem());
    }
  }

  private static IllegalStateException creationFailure(
      PrefabInstance instance, RuntimeException cause) {
    return new IllegalStateException(
        "Failed to create prefab '"
            + instance.name()
            + "' of type '"
            + instance.type()
            + "': "
            + cause.getMessage(),
        cause);
  }
}
