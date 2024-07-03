package de.fwatermann.dungine.state;

import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.IVoidFunction;
import de.fwatermann.dungine.window.GameWindow;

/**
 * Represents a state of the game.
 */
public abstract class GameState implements Disposable {

  protected GameWindow window;

  /**
   * Create a new game state.
   * @param window the game window
   */
  protected GameState(GameWindow window) {
      this.window = window;
  }

  /**
   * Initialize this state. This method is called async to the render method.
   * If GL-Context is needed (e.g. for textures) use {@link GameWindow#runOnMainThread(IVoidFunction)}
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
   * Render this state.
   * @param deltaTime the time since the last frame in seconds
   */
  public abstract void render(float deltaTime);

  /**
   * Update this state. This method is called async to the render method.
   * @param deltaTime the time since the last update in seconds
   */
  public void update(float deltaTime) {}

}
