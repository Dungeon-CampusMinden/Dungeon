package contrib.hud.image;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed image dialog. */
public final class ShowImageDialogBuilder {

  private ShowImageDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
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

    return new OverlayUiNodeHandle(
      new ShowImageOverlay(
        imagePath, speed, maxSize, imageText, imageTextScale, imageTextColorRgba8888));
  }
}
