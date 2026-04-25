package contrib.hud.elements.bars;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/**
 * A builder for creating attribute bar dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display an attribute bar overlay.
 *
 * <p>It retrieves bar configuration from the dialog context and positions the bar relative to a specific entity.
 */
public final class AttributeBarOverlayBuilder {

  private AttributeBarOverlayBuilder() {}

  /**
   * Builds a UI node handle for an attribute bar overlay.
   *
   * <p>This method requires the dialog context to contain an AttributeBarDialogData object
   * that specifies the bar's style, position, and vertical offset.
   *
   * <p>The bar is positioned relative to the entity specified in the data and made visible.
   *
   * @param ctx the dialog context containing the attribute bar configuration
   * @return a UI node handle wrapping the created attribute bar overlay
   * @throws IllegalArgumentException if the required attribute bar data is not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    AttributeBarDialogData data =
      ctx.require(DialogContextKeys.ATTRIBUTE_BAR, AttributeBarDialogData.class);

    AttributeBarOverlay overlay =
      new AttributeBarOverlay(data.styleName());

    AttributeBarUtil.updatePosition(overlay, data.pc(), data.verticalOffset());
    overlay.setVisible(true);

    return new OverlayHandle(overlay);
  }
}
