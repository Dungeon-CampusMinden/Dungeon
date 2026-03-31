package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.utils.components.showImage.TransitionSpeed;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed image dialog. */
public final class LitiengineShowImageDialogBuilder {

  private LitiengineShowImageDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    String imagePath = ctx.require(DialogContextKeys.IMAGE, String.class);
    TransitionSpeed speed =
      ctx.find(DialogContextKeys.IMAGE_TRANSITION_SPEED, TransitionSpeed.class)
        .orElse(TransitionSpeed.MEDIUM);

    return new LitiengineUiNodeHandle(new LitiengineShowImageOverlay(imagePath, speed));
  }
}
