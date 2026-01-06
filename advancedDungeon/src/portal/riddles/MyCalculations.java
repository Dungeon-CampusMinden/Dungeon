package portal.riddles;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import portal.portals.abstraction.Calculations;
import portal.portals.abstraction.PortalUtils;
import portal.util.Tools;

public class MyCalculations extends Calculations {

  public Point calculatePortalExit(Entity portal) {
    Entity otherPortal;
    if (portal.name().equals(PortalUtils.BLUE_PORTAL_NAME))
      otherPortal = Tools.getPortal(PortalUtils.GREEN_PORTAL_NAME);
    else otherPortal = Tools.getPortal(PortalUtils.BLUE_PORTAL_NAME);
    PositionComponent pc = Tools.getPositionComponent(otherPortal);
    Direction direction = pc.viewDirection();
    return pc.position().translate(direction);
  }
}
