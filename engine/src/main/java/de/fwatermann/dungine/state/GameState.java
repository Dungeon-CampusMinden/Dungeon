package de.fwatermann.dungine.state;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ui.UIRoot;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.IVoidFunction;
import de.fwatermann.dungine.window.GameWindow;

/** Represents a state of the game. It extents the ECS class. */
public abstract class GameState extends ECS implements Disposable {

  protected GameWindow window;
  protected float lastFrameDeltaTime = 0.0f;
  protected float lastTickDeltaTime = 0.0f;

  protected UIRoot ui;

  /**
   * Create a new game state.
   *
   * @param window the game window
   */
  protected GameState(GameWindow window) {
    this.window = window;
    this.ui = new UIRoot(this.window, this.window.size().x, this.window.size().y);
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
    this.lastFrameDeltaTime = deltaTime;
    this.executeSystems(this, true);
    this.renderState(deltaTime);
    this.ui.render();
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
    this.lastTickDeltaTime = deltaTime;
    this.executeSystems(this, false);
    this.updateState(deltaTime);
  }

  /**
   * Update this state. This method is called async to the render method by {@link #update(float)}
   *
   * @param deltaTime the time since the last update in seconds
   */
  public void updateState(float deltaTime) {}

  /**
   * Get the last delta time of the last frame.
   *
   * @return the last delta time of the last frame in seconds
   */
  public float lastFrameDeltaTime() {
    return this.lastFrameDeltaTime;
  }

  /**
   * Get the last delta time of the last tick.
   *
   * @return the last delta time of the last tick in seconds
   */
  public float lastTickDeltaTime() {
    return this.lastTickDeltaTime;
  }

  /**
   * Get the game window.
   *
   * @return the game window
   */
  public GameWindow window() {
    return this.window;
  }
}
