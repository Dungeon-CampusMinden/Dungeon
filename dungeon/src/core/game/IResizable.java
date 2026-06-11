package core.game;

/**
 * Interface for game components that need to respond to window resize events. Implementing classes
 * should define the onResize method to adjust their layout or rendering based on the new window
 * dimensions.
 */
public interface IResizable {

  /**
   * Called when the game window is resized.
   *
   * @param width The new width of the game window
   * @param height The new height of the game window
   */
  void onResize(int width, int height);
}
