package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.elements.AttributeBarDialogData;
import contrib.hud.utils.AttributeBarUtil;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed progress bar dialog used for entity attribute bars. */
public final class LitiengineProgressBarDialogBuilder {

  private LitiengineProgressBarDialogBuilder() {}

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
