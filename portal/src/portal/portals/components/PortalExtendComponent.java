package portal.portals.components;

import core.Component;
import core.Entity;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import java.util.function.Consumer;

/** Helper component for the PortalExtendSystem. */
public class PortalExtendComponent implements Component {
  private boolean throughBlue = false;
  private boolean throughGreen = false;
  private boolean isExtended = false;

  /** Default extend method. */
  public TriConsumer<Direction, Point, PortalExtendComponent> onExtend = (d, p, pec) -> {};

  /** Default trim method. */
  public Consumer<Entity> onTrim = (e) -> {};

  /**
   * Returns the state of throughGreen.
   *
   * @return true if the holding entity is colliding with the green portal, otherwise false.
   */
  public boolean isThroughGreen() {
    return throughGreen;
  }

  /**
   * Returns the state of throughBlue.
   *
   * @return true if the holding entity is colliding with the blue portal, otherwise false.
   */
  public boolean isThroughBlue() {
    return throughBlue;
  }

  /**
   * Returns the state of isExtended.
   *
   * @return true if the entity is being extended, otherwise false.
   */
  public boolean isExtended() {
    return isExtended;
  }

  /**
   * Sets the state of isExtended.
   *
   * @param extended Sets the state of isExtended.
   */
  public void setExtended(boolean extended) {
    isExtended = extended;
  }

  /**
   * Sets the state of throughBlue.
   *
   * @param throughBlue Sets the state of throughBlue.
   */
  public void setThroughBlue(boolean throughBlue) {
    this.throughBlue = throughBlue;
  }

  /**
   * Sets the state of throughGreen.
   *
   * @param throughGreen Sets the state of throughGreen.
   */
  public void setThroughGreen(boolean throughGreen) {
    this.throughGreen = throughGreen;
  }
}
