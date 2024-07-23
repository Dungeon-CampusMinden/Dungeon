package de.fwatermann.dungine.state;

import de.fwatermann.dungine.ecs.AsyncSystem;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.IVoidFunction;
import de.fwatermann.dungine.window.GameWindow;

import java.util.*;
import java.util.stream.Stream;

/** Represents a state of the game. */
public abstract class GameState implements Disposable {

  protected GameWindow window;

  private final Set<Entity<?>> entities = new HashSet<>();
  private final Map<System<?>, Integer> syncSystems = new HashMap<>();
  private final Map<AsyncSystem<?>, Integer> asyncSystems = new HashMap<>();

  /**
   * Create a new game state.
   *
   * @param window the game window
   */
  protected GameState(GameWindow window) {
    this.window = window;
  }

  /**
   * Initialize this state. This method is called async to the render method. If GL-Context is
   * needed (e.g. for textures) use {@link GameWindow#runOnMainThread(IVoidFunction)}
   */
  public abstract void init();

  /**
   * Get the progress of this state. This is used for loading screens.
   *
   * <p>Default implementation returns 0.
   *
   * @return a value between 0 and 1
   */
  public float getProgress() {
    return 0.0f;
  }

  /**
   * Check if this state is loaded.
   *
   * @return true if this state is loaded
   */
  public abstract boolean loaded();

  /**
   * Render this state.
   *
   * @param deltaTime the time since the last frame in seconds
   */
  public final void render(float deltaTime) {
    this.executeSyncSystems();
    this.renderState(deltaTime);
  }

  /**
   * Render this State. This method is called by {@link #render(float)}
   *
   * @param deltaTime The time since the last frame in seconds
   */
  public void renderState(float deltaTime) {}

  /**
   * Update this state. This method is called async to the render method.
   *
   * @param deltaTime the time since the last update in seconds
   */
  public final void update(float deltaTime) {
    this.executeAsyncSystem();
    this.updateState(deltaTime);
  }

  /**
   * Update this state. This method is called async to the render method by {@link #update(float)}
   *
   * @param deltaTime the time since the last update in seconds
   */
  public void updateState(float deltaTime) {}

  /**
   * Add an entity to the game.
   *
   * @param entity Entity to be added
   * @return true if the entity was added successfully, false if the entity was already added.
   */
  public boolean addEntity(Entity<?> entity) {
    return this.entities.add(entity);
  }

  /**
   * Remove an entity from the game.
   *
   * @param entity Entity to be removed
   * @return true if entity was removed successfully, false if the entity was not in the game.
   */
  public boolean removeEntity(Entity<?> entity) {
    return this.entities.remove(entity);
  }

  /**
   * Check if an entity is currently in the game.
   *
   * @param entity Entity to be checked
   * @return true if the entity is in the game, otherwise false.
   */
  public boolean containsEntity(Entity<?> entity) {
    return this.entities.contains(entity);
  }

  /**
   * Get a stream of all entities currently contained in the game.
   *
   * @return Stream of entities
   */
  public Stream<Entity<?>> entities() {
    return this.entities.stream();
  }

  /**
   * Add a system to the game.
   *
   * <p>Systems that extend the class {@link AsyncSystem} will be executed asynchronous to the
   * render thread.
   *
   * <p>Newly added systems will be executed in the next tick/frame, then every interval tick/frame.
   *
   * @param system System to be added.
   */
  public void addSystem(System<?> system) {
    if (system instanceof AsyncSystem<?> asyncSystem) {
      this.asyncSystems.put(asyncSystem, 0);
    } else {
      this.syncSystems.put(system, 0);
    }
  }

  /**
   * Remove a system from the game.
   */
  public void removeSystem(System<?> system) {
    if(system instanceof AsyncSystem<?> asyncSystem) {
      this.asyncSystems.remove(asyncSystem);
    } else {
      this.syncSystems.remove(system);
    }
  }

  /**
   * Check whether the game has a system.
   */
  public boolean containsSystem(System<?> system) {
    if(system instanceof AsyncSystem<?> asyncSystem) {
      return this.asyncSystems.containsKey(asyncSystem);
    } else {
      return this.syncSystems.containsKey(system);
    }
  }

  /**
   * Get a stream of all synchronous systems.
   * @return Stream of systems
   */
  public Stream<System<?>> syncSystems() {
    return this.syncSystems.keySet().stream();
  }

  /**
   * Get a stream of all asynchronous systems.
   * @return Stream of systems
   */
  public Stream<AsyncSystem<?>> asyncSystems() {
    return this.asyncSystems.keySet().stream();
  }

  /**
   * Get a stream of all (asynchronous & synchronous) systems.
   * @return Stream of systems
   */
  public Stream<System<?>> streams() {
    return Stream.concat(this.syncSystems.keySet().stream(), this.asyncSystems.keySet().stream());
  }

  private void executeSyncSystems() {
    this.syncSystems
        .entrySet()
        .forEach(
            e -> {
              e.setValue(e.getValue() - 1);
              if (e.getValue() <= 0) {
                e.getKey().update();
                e.setValue(e.getKey().interval());
              }
            });
  }

  private void executeAsyncSystem() {
    this.asyncSystems
        .entrySet()
        .forEach(
            e -> {
              e.setValue(e.getValue() - 1);
              if (e.getValue() <= 0) {
                e.getKey().update();
                e.setValue(e.getKey().interval());
              }
            });
  }
}
