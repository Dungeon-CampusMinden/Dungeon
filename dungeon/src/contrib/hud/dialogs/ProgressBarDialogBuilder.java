package contrib.hud.dialogs;

import contrib.hud.elements.AttributeBarDialogData;
import contrib.hud.elements.AttributeBarOverlay;
import contrib.hud.utils.AttributeBarUtil;
import core.ui.UiNodeHandle;
import core.ui.overlay.OverlayUiNodeHandle;

/**
 * A builder for creating progress bar dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display an attribute bar overlay
 * (such as a progress bar).
 *
 * <p>It retrieves bar configuration from the dialog context and positions the bar relative to a specific entity.
 */
public final class ProgressBarDialogBuilder {

  private ProgressBarDialogBuilder() {}

  /**
   * Builds a UI node handle for a progress bar overlay.
   *
   * <p>This method requires the dialog context to contain an AttributeBarDialogData object
   * that specifies the bar's style, position, and vertical offset.
   *
   * <p>The bar is positioned relative to the entity specified in the data and made visible.
   *
   * @param ctx the dialog context containing the progress bar configuration
   * @return a UI node handle wrapping the created attribute bar overlay
   * @throws IllegalArgumentException if the required progress bar data is not present in the context
   */
  public static UiNodeHandle build(DialogContext ctx) {
    AttributeBarDialogData data =
      ctx.require(DialogContextKeys.PROGRESS_BAR, AttributeBarDialogData.class);

    AttributeBarOverlay overlay =
      new AttributeBarOverlay(data.styleName());

    AttributeBarUtil.updatePosition(overlay, data.pc(), data.verticalOffset());
    overlay.setVisible(true);

    return new OverlayUiNodeHandle(overlay);
  }
}
