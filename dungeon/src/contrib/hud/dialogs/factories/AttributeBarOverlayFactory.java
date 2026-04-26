package contrib.hud.dialogs.factories;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.elements.bars.AttributeBarLayout;
import contrib.hud.elements.bars.AttributeBarOverlay;
import contrib.hud.elements.bars.AttributeBarOverlayData;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/**
 * Factory class for creating attribute bar overlay UI handles.
 *
 * <p>This class is responsible for constructing and configuring attribute bar overlays based on
 * data provided in a {@link DialogContext}.
 *
 * <p>Attribute bar overlays are UI components displayed relative to a specified entity, styled,
 * and positioned according to the configuration in the context.
 *
 * <p>The overlays are created as visible elements ready for interaction.
 *
 * <p>This class cannot be instantiated, as it only provides static utility methods.
 */
public final class AttributeBarOverlayFactory {

  private AttributeBarOverlayFactory() {}

  /**
   * Builds a UI node handle for an attribute bar overlay.
   *
   * <p>This method requires the dialog context to contain an AttributeBarDialogData object that
   * specifies the bar's style, position, and vertical offset.
   *
   * <p>The bar is positioned relative to the entity specified in the data and made visible.
   *
   * @param ctx the dialog context containing the attribute bar configuration
   * @return a UI node handle wrapping the created attribute bar overlay
   * @throws IllegalArgumentException if the required attribute bar data is not present in the
   *     context
   */
  public static UiHandle build(DialogContext ctx) {
    AttributeBarOverlayData data =
        ctx.require(DialogContextKeys.ATTRIBUTE_BAR, AttributeBarOverlayData.class);

    AttributeBarOverlay overlay = new AttributeBarOverlay(data.styleName());

    AttributeBarLayout.updatePosition(overlay, data.pc(), data.verticalOffset());
    overlay.setVisible(true);

    return new OverlayHandle(overlay);
  }
}
