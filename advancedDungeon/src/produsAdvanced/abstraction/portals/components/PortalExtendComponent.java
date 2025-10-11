package produsAdvanced.abstraction.portals.components;

import core.Component;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;

import java.util.Objects;
import java.util.function.Consumer;

public class PortalExtendComponent implements Component {
  public boolean throughBlue;
  public boolean throughGreen;
  public boolean isExtended = false;

  // Default extend method
  public TriConsumer<Direction, Point, PortalExtendComponent> onExtend = (d, p, pec) -> {};

  // Default trim method
  public Consumer<Entity> onTrim = (e) -> {};

  public boolean checkGreen() {
    return throughGreen;
  }

  public boolean checkBlue() {
    return throughBlue;
  }

  public boolean isExtended() {
    return isExtended;
  }
}
