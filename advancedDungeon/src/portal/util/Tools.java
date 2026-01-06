package portal.util;

import core.Entity;
import core.components.PositionComponent;
import portal.portals.abstraction.PortalUtils;

public class Tools {

  public static PositionComponent getPositionComponent(Entity entity) {
    return entity.fetch(PositionComponent.class).orElse(null);
  }

  public static Entity getPortal(String portalName) {
    if (portalName.equals(PortalUtils.BLUE_PORTAL_NAME))
      return PortalUtils.getBluePortal().orElse(null);
    else return PortalUtils.getGreenPortal().orElse(null);
  }
}
