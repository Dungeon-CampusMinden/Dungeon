package portal.portals;

import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import portal.portals.components.PortalComponent;
import portal.portals.components.PortalExtendComponent;
import portal.riddles.utils.PortalUtils;

/**
 * The PortalExtendSystem manages the interaction with portals for entities that need to be extended
 * instead of being teleported.
 *
 * <p>It calls the {@link PortalExtendComponent} onExtend for all the entities that have the
 * component and where it's not extended yet.
 */
public class PortalExtendSystem extends System {

  /** Constructs a new PortalExtendSystem. */
  public PortalExtendSystem() {
    super(PortalExtendComponent.class);
  }

  /**
   * Executes the applyPortalExtendLogic method for all the entities that have a portal extend
   * component and which aren't extended yet.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .map(this::buildDataObject)
        .filter(pec -> !pec.isExtended())
        .forEach(this::applyPortalExtendLogic);
  }

  /**
   * Extracts the portal component out of the given entity and builds the data with it.
   *
   * @param entity Entity to extract the portal extend component.
   * @return the
   */
  private PortalExtendComponent buildDataObject(Entity entity) {

    return entity
        .fetch(PortalExtendComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, PortalExtendComponent.class));
  }

  /**
   * Calls the onExtend method which should be overwritten in the specific classes.
   *
   * @param pec Data which holds the {@link PortalExtendComponent} from which the extent will be
   *     called.
   */
  private void applyPortalExtendLogic(PortalExtendComponent pec) {
    if (pec.isThroughBlue()) {
      PortalUtils.getGreenPortal()
          .ifPresent(
              portal -> {
                portal
                    .fetch(PortalComponent.class)
                    .ifPresent(
                        pc -> {
                          Entity other =
                              PortalUtils.getBluePortal()
                                  .flatMap(bluePortal -> bluePortal.fetch(PortalComponent.class))
                                  .get()
                                  .getExtendedEntityThrough();
                          if (pc.getExtendedEntityThrough() == null) {
                            pc.setExtendedEntityThrough(other);
                          }
                        });
                PositionComponent greenPortalPosition = portal.fetch(PositionComponent.class).get();
                pec.onExtend.accept(
                    greenPortalPosition.viewDirection(), greenPortalPosition.position(), pec);
                pec.setExtended(true);
              });
    } else if (pec.isThroughGreen()) {
      PortalUtils.getBluePortal()
          .ifPresent(
              portal -> {
                portal
                    .fetch(PortalComponent.class)
                    .ifPresent(
                        pc -> {
                          Entity other =
                              PortalUtils.getGreenPortal()
                                  .flatMap(greenPortal -> greenPortal.fetch(PortalComponent.class))
                                  .get()
                                  .getExtendedEntityThrough();
                          if (pc.getExtendedEntityThrough() == null) {
                            pc.setExtendedEntityThrough(other);
                          }
                        });
                PositionComponent bluePortalPosition = portal.fetch(PositionComponent.class).get();
                pec.onExtend.accept(
                    bluePortalPosition.viewDirection(), bluePortalPosition.position(), pec);
                pec.setExtended(true);
              });
    }
  }
}
