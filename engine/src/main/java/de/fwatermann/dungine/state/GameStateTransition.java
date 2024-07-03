package de.fwatermann.dungine.state;

import de.fwatermann.dungine.window.GameWindow;

/**
 * Represents a transition between two game states. This should not take long to initialize because
 * it is used between two states. E.G Loading screen. This is not a state itself. Also transitions
 * will be reused.
 */
public abstract class GameStateTransition {

  protected GameWindow window;

  /**
   * Create a new transition.
   * @param window the game window
   */
  protected GameStateTransition(GameWindow window) {
      this.window = window;
  }

  /**
   * Create a new transition.
   */
  protected GameStateTransition() {}

  /** Initialize this transition. */
  public abstract void init();

  /**
   * Render this transition.
   *
   * @param deltaTime the time since the last frame in seconds
   */
  public abstract void render(float deltaTime);

  /**
   * Reset this transition.
   *
   * <p>Default implementation does nothing.
   */
  public void reset() {}



}
