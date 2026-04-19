package contrib.hud.dialogs.builders;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.elements.bars.AttributeBarDialogData;
import contrib.hud.elements.bars.AttributeBarOverlay;
import contrib.hud.utils.AttributeBarUtil;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

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
  public static UiHandle build(DialogContext ctx) {
    AttributeBarDialogData data =
      ctx.require(DialogContextKeys.PROGRESS_BAR, AttributeBarDialogData.class);

    AttributeBarOverlay overlay =
      new AttributeBarOverlay(data.styleName());

    AttributeBarUtil.updatePosition(overlay, data.pc(), data.verticalOffset());
    overlay.setVisible(true);

    return new OverlayHandle(overlay);
  }
}
