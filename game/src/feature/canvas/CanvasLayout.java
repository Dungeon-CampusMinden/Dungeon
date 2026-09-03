package feature.canvas;

import java.io.Serializable;
import java.util.Objects;

/**
 * Serializable visual configuration needed to render a canvas without its local definition.
 *
 * @param title the window title
 * @param areaWidth the preferred viewport width
 * @param areaHeight the preferred viewport height
 * @param options the canvas interaction and rendering options
 * @param showResetViewButton whether the reset-view button is visible
 * @param showFitButton whether the fit-to-content button is visible
 */
public record CanvasLayout(
    String title,
    float areaWidth,
    float areaHeight,
    CanvasOptions options,
    boolean showResetViewButton,
    boolean showFitButton)
    implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Validates and normalizes the layout. */
  public CanvasLayout {
    title = title == null ? "" : title;
    if (areaWidth <= 0f || areaHeight <= 0f) {
      throw new IllegalArgumentException("area size must be positive");
    }
    Objects.requireNonNull(options, "options");
  }

  /**
   * Creates the default canvas layout.
   *
   * @return a layout with the standard canvas dimensions and options
   */
  public static CanvasLayout defaults() {
    return new CanvasLayout("", 900f, 560f, new CanvasOptions(), true, true);
  }
}
