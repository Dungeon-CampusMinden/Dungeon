package core.ui;

/**
 * A handle for interacting with the stage or main UI container of the game.
 *
 * <p>This interface provides methods to access the underlying stage object, retrieve its dimensions,
 * and query the current mouse position relative to the stage.
 *
 * <p>It serves as an abstraction layer over the specific UI framework used by the game, allowing for
 * consistent interaction with the stage across different implementations.
 */
public interface StageHandle {

  /**
   * Gets the width of the stage.
   *
   * @return the stage width in pixels (or framework units)
   */
  float getWidth();

  /**
   * Gets the height of the stage.
   *
   * @return the stage height in pixels (or framework units)
   */
  float getHeight();

  /**
   * Gets the current mouse x-coordinate.
   *
   * @return the x-coordinate of the mouse cursor in stage coordinates
   */
  int mouseX();

  /**
   * Gets the current mouse y-coordinate.
   *
   * @return the y-coordinate of the mouse cursor in stage coordinates
   */
  int mouseY();
}
