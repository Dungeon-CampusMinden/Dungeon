package contrib.hud.elements.bars;

/**
 * Engine-agnostic handle for an attribute bar widget.
 *
 * <p>This keeps HUD systems independent of the concrete UI toolkit.
 */
public interface AttributeBarHandle {

  /**
   * Removes the bar from the UI.
   */
  void remove();

  /**
   * Updates the visibility of the bar.
   *
   * @param visible true if the bar should be visible
   */
  void setVisible(boolean visible);

  /**
   * Updates the screen/stage position of the bar.
   *
   * @param x x-position in stage coordinates
   * @param y y-position in stage coordinates
   */
  void setPosition(float x, float y);

  /**
   * Updates the current normalized value of the bar.
   *
   * @param value normalized value in range [0, 1]
   */
  void setValue(float value);
}
