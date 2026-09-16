package feature.prefabs;

import engine.Entity;
import engine.Game;
import engine.level.elements.ILevel;
import feature.components.ShowImageComponent;
import feature.level.visibility.LevelHideComponent;
import feature.level.visibility.LevelHideSystem;
import feature.systems.ShowImageSystem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Creates authored prefab instances for one runtime side. */
public final class PrefabSpawner {

  private static final Set<Entity> CLIENT_ENTITIES = new LinkedHashSet<>();

  private PrefabSpawner() {}

  /**
   * Creates and adds all prefabs assigned to the requested side.
   *
   * @param level owning level
   * @param side side to instantiate
   */
  public static void spawn(ILevel level, PrefabSide side) {
    PrefabCreationContext context = new PrefabCreationContext(level, side);
    for (PrefabInstance source : level.prefabs()) {
      Prefab prefab = PrefabRegistry.require(source.type());
      if (prefab.side() != side) continue;

      PrefabInstance instance = prefab.normalize(source);
      List<Entity> created;
      try {
        created = List.copyOf(prefab.create(context, instance));
      } catch (RuntimeException e) {
        throw creationFailure(instance, e);
      }

      List<Entity> added = new ArrayList<>();
      try {
        ensureRequiredSystems(created);
        for (Entity entity : created) {
          Game.add(entity);
          added.add(entity);
        }
        if (side == PrefabSide.CLIENT) CLIENT_ENTITIES.addAll(added);
      } catch (RuntimeException e) {
        added.forEach(Game::remove);
        throw creationFailure(instance, e);
      }
    }
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
    if (side != PrefabSide.CLIENT) return;
    CLIENT_ENTITIES.stream().filter(PrefabSpawner::isPresent).toList().forEach(Game::remove);
    CLIENT_ENTITIES.clear();
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
