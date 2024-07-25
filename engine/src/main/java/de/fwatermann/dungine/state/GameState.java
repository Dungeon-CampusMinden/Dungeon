package de.fwatermann.dungine.state;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.IVoidFunction;
import de.fwatermann.dungine.window.GameWindow;

/** Represents a state of the game. */
public abstract class GameState extends ECS implements Disposable {

  protected GameWindow window;

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
    this.executeSystems(this, true);
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
    this.executeSystems(this, false);
    this.updateState(deltaTime);
  }

  /**
   * Update this state. This method is called async to the render method by {@link #update(float)}
   *
   * @param deltaTime the time since the last update in seconds
   */
  public void updateState(float deltaTime) {}
}
