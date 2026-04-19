package contrib.hud.showimage;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/**
 * A builder for creating image display dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display an image dialog overlay.
 *
 * <p>It retrieves image configuration from the dialog context, including the image path,
 * transition speed, size constraints, and optional overlay text.
 */
public final class ShowImageDialogBuilder {

  private ShowImageDialogBuilder() {}

  /**
   * Builds a UI node handle for an image display dialog overlay.
   *
   * <p>This method retrieves the required image path and optional configuration parameters
   * from the dialog context.
   *
   * <p>If optional parameters are not provided, sensible defaults are used:
   * <ul>
   *   <li>Transition speed defaults to MEDIUM</li>
   *   <li>The maximum size defaults to 0.85 (85% of screen)</li>
   *   <li>Image text defaults to null (no text overlay)</li>
   *   <li>Text scale defaults to 1.0</li>
   *   <li>Text color defaults to black with full opacity (0x000000ff)</li>
   * </ul>
   *
   * @param ctx the dialog context containing the image path and optional configuration
   * @return a UI node handle wrapping the created image display overlay
   * @throws IllegalArgumentException if the required image path is not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    String imagePath = ctx.require(DialogContextKeys.IMAGE, String.class);
    TransitionSpeed speed =
      ctx.find(DialogContextKeys.IMAGE_TRANSITION_SPEED, TransitionSpeed.class)
        .orElse(TransitionSpeed.MEDIUM);

    float maxSize =
      ctx.find(DialogContextKeys.IMAGE_MAX_SIZE, Float.class).orElse(0.85f);

    String imageText =
      ctx.find(DialogContextKeys.IMAGE_TEXT, String.class).orElse(null);

    float imageTextScale =
      ctx.find(DialogContextKeys.IMAGE_TEXT_SCALE, Float.class).orElse(1f);

    int imageTextColorRgba8888 =
      ctx.find(DialogContextKeys.IMAGE_TEXT_COLOR_RGBA8888, Integer.class).orElse(0x000000ff);

    return new OverlayHandle(
      new ShowImageDialogOverlay(
        imagePath, speed, maxSize, imageText, imageTextScale, imageTextColorRgba8888));
  }
}
