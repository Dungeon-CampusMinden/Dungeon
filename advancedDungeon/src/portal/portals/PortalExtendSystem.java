package portal.portals;

import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import portal.portals.components.PortalComponent;
import portal.portals.components.PortalExtendComponent;
import portal.riddles.utils.PortalUtils;

import java.util.Optional;

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
      PortalUtils.getGreenPortal().ifPresent(exit ->
        PortalUtils.getBluePortal().ifPresent(entry ->
          applyExtend(exit, entry, pec)
        )
      );
    } else if (pec.isThroughGreen()) {
      PortalUtils.getBluePortal().ifPresent(exit ->
        PortalUtils.getGreenPortal().ifPresent(entry ->
          applyExtend(exit, entry, pec)
        )
      );
    }
  }

  private void applyExtend(Entity exitPortal,Entity entryPortal, PortalExtendComponent pec) {
    Optional<PortalComponent> portalComponent =  exitPortal.fetch(PortalComponent.class);
    Optional<PortalComponent> otherPortalComponent = entryPortal.fetch(PortalComponent.class);

    Optional<Entity> other = otherPortalComponent.map(PortalComponent::getExtendedEntityThrough);

    portalComponent.ifPresent(pc ->
      other.ifPresent(pc::setExtendedEntityThrough)
    );

    exitPortal.fetch(PositionComponent.class).ifPresent(position -> {
      pec.onExtend.accept(position.viewDirection(), position.position(), pec);
      pec.setExtended(true);
    });


  }


}
