package portal.portals;

import core.Entity;
import portal.portals.components.PortalComponent;
import portal.portals.components.PortalExtendComponent;
import portal.riddles.utils.PortalUtils;

import java.util.Optional;

public class PortalExtendHandler {

  /**
   * Trims an extended entity by calling its {@link PortalExtendComponent} onTrim Consumer.
   *
   * @param portal The portal the entity is entering at first.
   * @param other The entity that is being extended.
   */
  public static void clearExtendedEntity(Entity portal, Entity other) {
    // TODO: warum geht das, die portalextend component wird doch immer zum collider hinzugefügt und nicht zur Entity direkt
    other
      .fetch(PortalExtendComponent.class)
      .ifPresent(
        pec -> {
          if (pec.isExtended()) {
            pec.onTrim.accept(other);
            pec.setExtended(false);
          }
          resetPortal(portal, PortalColor.GREEN, pec);
          resetPortal(portal, PortalColor.BLUE, pec);
        });
  }

  private static void resetPortal(Entity portal, PortalColor color, PortalExtendComponent pec) {
    Optional<Entity> portalToCheck = color == PortalColor.BLUE
      ? PortalUtils.getBluePortal()
      : PortalUtils.getGreenPortal();

    portalToCheck.ifPresent(p -> {
      if (p == portal) {  // ← gleicher Check wie vorher
        if (color == PortalColor.GREEN) pec.setThroughGreen(false);
        else pec.setThroughBlue(false);
        p.fetch(PortalComponent.class)
          .ifPresent(pc -> pc.setExtendedEntityThrough(null));
      }
    });
  }

}
