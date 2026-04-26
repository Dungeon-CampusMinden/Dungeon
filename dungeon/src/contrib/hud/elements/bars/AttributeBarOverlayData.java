package contrib.hud.elements.bars;

import core.components.PositionComponent;
import java.io.Serial;
import java.io.Serializable;

/**
 * A record that encapsulates data for configuring an attribute bar dialog in the HUD.
 *
 * <p>This record is used to pass configuration parameters when creating or displaying an attribute
 * bar dialog element. It implements {@link Serializable} to allow for data persistence and
 * serialization.
 *
 * @param pc The position component of the entity to which the bar is attached.
 * @param styleName The name of the style to apply to the bar dialog.
 * @param verticalOffset The vertical offset to apply to the bar dialog, allowing it to be
 *     positioned above or below the entity as needed.
 */
public record AttributeBarOverlayData(PositionComponent pc, String styleName, float verticalOffset)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
