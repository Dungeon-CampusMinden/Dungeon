package de.fwatermann.dungine.ecs;

import de.fwatermann.dungine.utils.ThreadUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Abstract class representing the core of the Entity Component System (ECS) architecture. This
 * class facilitates the management of entities and systems within the application, allowing for the
 * addition, removal, and execution of systems and entities.
 */
public abstract class ECS {

  /** Set holding all entities within the ECS */
  protected final Set<Entity> entities = new HashSet<>();

  /** Map holding all systems withing the ECS, associated each system with their interval */
  protected final Map<System<?>, Integer> systems = new HashMap<>();

  /**
   * Adds an entity to the ECS.
   *
   * @param entity The entity to be added.
   */
  public void addEntity(Entity entity) {
    this.entities.add(entity);
    this.systems.keySet().stream()
        .filter(s -> entity.hasComponents(s.componentFilter()))
        .forEach(s -> s.onEntityAdd(this, entity));
  }

  /**
   * Removes an entity from the ECS.
   *
   * @param entity The entity to be removed.
   */
  public void removeEntity(Entity entity) {
    this.entities.remove(entity);
    this.systems.keySet().stream()
        .filter(s -> entity.hasComponents(s.componentFilter()))
        .forEach(s -> s.onEntityRemove(this, entity));
  }

  /**
   * Checks if an entity is present in the ECS.
   *
   * @param entity The entity to check for.
   * @return true if the entity is present, false otherwise.
   */
  public boolean hasEntity(Entity entity) {
    return this.entities.contains(entity);
  }

  /**
   * Returns a stream of all entities in the ECS.
   *
   * @return A stream of all entities.
   */
  public Stream<Entity> entities() {
    return this.entities.stream();
  }

  /**
   * Returns a stream of all entities in the ECS that have all of the given components.
   *
   * @param ComponentClasses The components to filter by.
   * @return A stream of all entities that have all of the given components.
   */
  @SafeVarargs
  public final Stream<Entity> entities(Class<? extends Component>... ComponentClasses) {
    return this.entities.stream()
        .filter(
            e -> {
              for (int i = 0; i < ComponentClasses.length; i++) {
                if (!e.hasComponents(ComponentClasses[i])) return false;
              }
              return true;
            });
  }

  /**
   * Adds a system to the ECS.
   *
   * @param system The system to be added.
   */
  public void addSystem(System<?> system) {
    this.systems.put(system, 0);
  }

  /**
   * Removes a system from the ECS.
   *
   * @param system The system to be removed.
   */
  public void removeSystem(System<?> system) {
    this.systems.remove(system);
  }

  /**
   * Checks if a system is present in the ECS.
   *
   * @param system The system to check for.
   * @return true if the system is present, false otherwise.
   */
  public boolean hasSystem(System<?> system) {
    return this.systems.containsKey(system);
  }

  /**
   * Returns a stream of all systems in the ECS.
   *
   * @return A stream of all systems.
   */
  public Stream<System<?>> systems() {
    return this.systems.keySet().stream();
  }

  /**
   * Executes all systems that match the given synchronization state.
   *
   * @param sync If true, executes synchronous systems; if false, executes asynchronous systems.
   */
  protected void executeSystems(ECS ecs, boolean sync) {
    if (sync && !ThreadUtils.isMainThread())
      throw new IllegalStateException(
          "Synchronous systems can only be executed on the main thread.");
    this.systems
        .entrySet()
        .forEach(
            e -> {
              if (e.getKey().sync() != sync || e.getKey().paused()) return;
              e.setValue(e.getValue() - 1);
              if (e.getValue() <= 0) {
                e.getKey().update(ecs);
                e.setValue(e.getKey().interval());
              }
            });
  }
}
