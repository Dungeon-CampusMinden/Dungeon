package de.fwatermann.dungine.ecs;

import de.fwatermann.dungine.utils.ThreadUtils;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * Abstract class representing the core of the Entity Component System (ECS) architecture. This
 * class facilitates the management of entities and systems within the application, allowing for the
 * addition, removal, and execution of systems and entities.
 */
public abstract class ECS {

  private final ReentrantReadWriteLock entityLock = new ReentrantReadWriteLock(true);
  private final ReentrantReadWriteLock systemLock = new ReentrantReadWriteLock(true);

  /** Set holding all entities within the ECS. */
  private final Set<Entity> entities = new HashSet<>();

  /** Map holding all systems withing the ECS, associated each system with their interval. */
  private final Map<System<?>, Integer> systems = new HashMap<>();

  /** Default constructor for the ECS class. */
  protected ECS() {}

  /**
   * Adds an entity to the ECS.
   *
   * @param entity The entity to be added.
   */
  public void addEntity(Entity entity) {
    try {
      this.entityLock.writeLock().lock();
      this.entities.add(entity);
    } finally {
      this.entityLock.writeLock().unlock();
    }
    try {
      this.systemLock.readLock().lock();
      this.systems.keySet().stream()
          .filter(s -> entity.hasComponents(s.componentFilter()))
          .forEach(s -> s.onEntityAdd(this, entity));
    } finally {
      this.systemLock.readLock().unlock();
    }
  }

  /**
   * Adds a collection of entities to the ECS.
   *
   * @param entities The entities to be added.
   */
  public void addEntities(Collection<Entity> entities) {
    try {
      this.entityLock.writeLock().lock();
      this.entities.addAll(entities);
    } finally {
      this.entityLock.writeLock().unlock();
    }
    try {
      this.systemLock.readLock().lock();
      entities.forEach(
          e ->
              this.systems.keySet().stream()
                  .filter(s -> e.hasComponents(s.componentFilter()))
                  .forEach(s -> s.onEntityAdd(this, e)));
    } finally {
      this.systemLock.readLock().unlock();
    }
  }

  /**
   * Removes an entity from the ECS.
   *
   * @param entity The entity to be removed.
   */
  public void removeEntity(Entity entity) {
    try {
      this.entityLock.writeLock().lock();
      this.entities.remove(entity);
      this.systems.keySet().stream()
          .filter(s -> entity.hasComponents(s.componentFilter()))
          .forEach(s -> s.onEntityRemove(this, entity));
    } finally {
      this.entityLock.writeLock().unlock();
    }
  }

  /**
   * Checks if an entity is present in the ECS.
   *
   * @param entity The entity to check for.
   * @return true if the entity is present, false otherwise.
   */
  public boolean hasEntity(Entity entity) {
    try {
      this.entityLock.readLock().lock();
      return this.entities.contains(entity);
    } finally {
      this.entityLock.readLock().unlock();
    }
  }

  /**
   * Run a function receiving a stream of all entities in the ECS.
   *
   * @param func The function to run for each entity.
   */
  public void entities(IVoidFunction1P<Stream<Entity>> func) {
    try {
      this.entityLock.readLock().lock();
      func.run(this.entities.stream());
    } finally {
      this.entityLock.readLock().unlock();
    }
  }

  /**
   * Run a function receiving a stream of entities filtered by the given components.
   *
   * @param func The function to run for each entity.
   * @param componentFilter The components to filter by.
   */
  @SafeVarargs
  public final void entities(
      IVoidFunction1P<Stream<Entity>> func, Class<? extends Component>... componentFilter) {
    try {
      this.entityLock.readLock().lock();
      func.run(this.entities.stream().filter(e -> e.hasComponents(componentFilter)));
    } finally {
      this.entityLock.readLock().unlock();
    }
  }

  /**
   * Run a function for each entity in the ECS.
   *
   * @param func The function to run for each entity.
   */
  public final void forEachEntity(IVoidFunction1P<Entity> func) {
    try {
      this.entityLock.readLock().lock();
      this.entities.forEach(func::run);
    } finally {
      this.entityLock.readLock().unlock();
    }
  }

  /**
   * Run a function for each entity in the ECS filtered by the given components.
   *
   * @param func The function to run for each entity.
   * @param componentFilter The components to filter by.
   */
  @SafeVarargs
  public final void forEachEntity(
      IVoidFunction1P<Entity> func, Class<? extends Component>... componentFilter) {
    try {
      this.entityLock.readLock().lock();
      this.entities.stream().filter(e -> e.hasComponents(componentFilter)).forEach(func::run);
    } finally {
      this.entityLock.readLock().unlock();
    }
  }

  /**
   * Adds a system to the ECS.
   *
   * @param system The system to be added.
   */
  public void addSystem(System<?> system) {
    try {
      this.systemLock.writeLock().lock();
      this.systems.put(system, 0);
    } finally {
      this.systemLock.writeLock().unlock();
    }
  }

  /**
   * Removes a system from the ECS.
   *
   * @param system The system to be removed.
   */
  public void removeSystem(System<?> system) {
    try {
      this.systemLock.writeLock().lock();
      this.systems.remove(system);
    } finally {
      this.systemLock.writeLock().unlock();
    }
  }

  /**
   * Checks if a system is present in the ECS.
   *
   * @param system The system to check for.
   * @return true if the system is present, false otherwise.
   */
  public boolean hasSystem(System<?> system) {
    try {
      this.systemLock.readLock().lock();
      return this.systems.containsKey(system);
    } finally {
      this.systemLock.readLock().unlock();
    }
  }

  /**
   * Run a function receiving a stream of all systems in the ECS.
   *
   * @param func The function to run for each system.
   */
  public void systems(IVoidFunction1P<Stream<System<?>>> func) {
    try {
      this.systemLock.readLock().lock();
      func.run(this.systems.keySet().stream());
    } finally {
      this.systemLock.readLock().unlock();
    }
  }

  /**
   * Run a function for each system in the ECS.
   *
   * @param func The function to run for each system.
   */
  public void forEachSystem(IVoidFunction1P<System<?>> func) {
    try {
      this.systemLock.readLock().lock();
      this.systems.keySet().forEach(func::run);
    } finally {
      this.systemLock.readLock().unlock();
    }
  }

  /**
   * Executes all systems that match the given synchronization state.
   *
   * @param ecs The ECS instance.
   * @param sync If true, executes synchronous systems; if false, executes asynchronous systems.
   */
  protected void executeSystems(ECS ecs, boolean sync) {
    if (sync && !ThreadUtils.isMainThread())
      throw new IllegalStateException(
          "Synchronous systems can only be executed on the main thread.");
    try {
      this.systemLock.readLock().lock();
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
    } finally {
      this.systemLock.readLock().unlock();
    }
  }

  /**
   * Get the number of entities in this ECS-Environment.
   *
   * @return The number of entities.
   */
  public int entityCount() {
    try {
      this.entityLock.readLock().lock();
      return this.entities.size();
    } finally {
      this.entityLock.readLock().unlock();
    }
  }

  /**
   * Get the number of systems in this ECS-Environment.
   *
   * @return The number of systems.
   */
  public int systemCount() {
    try {
      this.systemLock.readLock().lock();
      return this.systems.size();
    } finally {
      this.systemLock.readLock().unlock();
    }
  }
}
