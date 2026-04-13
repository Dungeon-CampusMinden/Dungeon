package contrib.hud.dialogs;

import contrib.hud.elements.AttributeBarDialogData;
import contrib.hud.elements.LitiengineAttributeBarOverlay;
import contrib.hud.utils.AttributeBarUtil;
import core.ui.UiNodeHandle;
import core.ui.overlay.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed progress bar dialog used for entity attribute bars. */
public final class ProgressBarDialogBuilder {

  private ProgressBarDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    AttributeBarDialogData data =
      ctx.require(DialogContextKeys.PROGRESS_BAR, AttributeBarDialogData.class);

    LitiengineAttributeBarOverlay overlay =
      new LitiengineAttributeBarOverlay(data.styleName());

    AttributeBarUtil.updatePosition(overlay, data.pc(), data.verticalOffset());
    overlay.setVisible(true);

    return new LitiengineUiNodeHandle(overlay);
  }
}
