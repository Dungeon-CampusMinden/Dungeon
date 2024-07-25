package de.fwatermann.dungine.ecs;

import java.util.Set;

/**
 * Abstract class representing a system in the Entity Component System (ECS) architecture. Systems
 * are used to update entities in the ECS. Systems can be updated synchronously or asynchronously
 * and can be paused. Systems can also have a specific interval at which they are updated. Also
 * systems can be notified when an entity is added or removed from the ECS. The components provided
 * in the constructor are used to filter entities that are updated by this system, meaning this
 * system will only be notified about entities that have all the specified components. The update
 * method must be implemented by the user.
 *
 * @param <T> The type of the system, used for chaining methods.
 */
public abstract class System<T extends System<?>> {

  private int interval = 0;
  private final boolean isSync;
  private boolean paused = false;
  private final Set<Class<? extends Component>> components;

  /**
   * Create a new System with specific interval, sync flag and components.
   *
   * @param interval Interval at which this system should be updated.
   * @param isSync Whether this system should be updated synchronously or asynchronously to the
   *     render loop.
   * @param components List of components this system requires.
   */
  public System(int interval, boolean isSync, Set<Class<? extends Component>> components) {
    this.interval = interval;
    this.isSync = isSync;
    this.components = Set.copyOf(components);
  }

  /**
   * Create a new System with specific interval, sync flag and components.
   *
   * @param interval Interval at which this system should be updated.
   * @param isSync Whether this system should be updated synchronously or asynchronously to the
   *     render loop.
   * @param components List of components this system requires.
   */
  @SafeVarargs
  public System(int interval, boolean isSync, Class<? extends Component>... components) {
    this(interval, isSync, Set.of(components));
  }

  /**
   * Create a new System with specific interval.
   *
   * <p>The interval determines how many ticks should be waited before the next update trigger.
   *
   * @param interval Interval at which this system should be updated.
   */
  public System(int interval) {
    this(interval, false);
  }

  /**
   * Create a new System with default interval.
   *
   * <p>The interval determines how many ticks should be waited before the next update trigger.
   */
  public System() {
    this(1, false);
  }

  /**
   * Called when an entity is added to the ECS and matches the component filter of this system.
   *
   * <p>Default implementation does nothing.
   *
   * @param ecs The ECS instance this system is part of.
   * @param entity The entity that was added.
   */
  public void onEntityAdd(ECS ecs, Entity entity) {}

  /**
   * Called when an entity is removed from the ECS and matches the component filter of this system.
   *
   * <p>Default implementation does nothing.
   *
   * @param ecs The ECS instance this system is part of.
   * @param entity The entity that was removed.
   */
  public void onEntityRemove(ECS ecs, Entity entity) {}

  /**
   * Get the component filter for this system.
   *
   * <p>Default implementation does nothing.
   */
  public Set<Class<? extends Component>> componentFilter() {
    return this.components;
  }

  /**
   * Abstract method to update the system.
   *
   * @param ecs The ECS instance this system is part of, used to access entities and components.
   */
  public abstract void update(ECS ecs);

  public final int interval() {
    return this.interval;
  }

  public final T interval(int interval) {
    this.interval = interval;
    return (T) this;
  }

  public final boolean sync() {
    return this.isSync;
  }

  public final boolean paused() {
    return this.paused;
  }

  public final T paused(boolean paused) {
    this.paused = paused;
    return (T) this;
  }
}
