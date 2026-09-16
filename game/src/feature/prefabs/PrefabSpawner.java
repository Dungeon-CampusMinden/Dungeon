package feature.prefabs;

import engine.Entity;
import engine.Game;
import engine.level.elements.ILevel;
import feature.components.ShowImageComponent;
import feature.level.visibility.LevelHideComponent;
import feature.level.visibility.LevelHideSystem;
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
    } catch (RuntimeException e) {
      throw creationFailureWithCleanup(instance, prefab, context, added, tracked, e);
    }
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
    if (tracked != null) removeTracked(tracked, side);
    discardEmpty(level, bySide, side);
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
