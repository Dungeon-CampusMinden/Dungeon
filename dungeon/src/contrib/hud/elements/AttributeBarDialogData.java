package contrib.hud.elements;

import core.components.PositionComponent;
import java.io.Serial;
import java.io.Serializable;

/**
 * Engine-agnostic dialog data required to create an attribute bar UI node.
 */
public record AttributeBarDialogData(
  PositionComponent pc, String styleName, float verticalOffset) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
