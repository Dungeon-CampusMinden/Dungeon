package contrib.hud.dialogs;

import contrib.hud.elements.AttributeBarDialogData;
import contrib.hud.elements.AttributeBarOverlay;
import contrib.hud.utils.AttributeBarUtil;
import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed progress bar dialog used for entity attribute bars. */
public final class ProgressBarDialogBuilder {

  private ProgressBarDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
    AttributeBarDialogData data =
      ctx.require(DialogContextKeys.PROGRESS_BAR, AttributeBarDialogData.class);

    AttributeBarOverlay overlay =
      new AttributeBarOverlay(data.styleName());

    AttributeBarUtil.updatePosition(overlay, data.pc(), data.verticalOffset());
    overlay.setVisible(true);

    return new OverlayUiNodeHandle(overlay);
  }
}
