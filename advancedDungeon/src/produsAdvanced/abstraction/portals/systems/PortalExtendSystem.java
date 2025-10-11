package produsAdvanced.abstraction.portals.systems;

import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.components.MissingComponentException;
import produsAdvanced.abstraction.portals.PortalFactory;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;
import produsAdvanced.abstraction.portals.components.TractorBeamComponent;

public class PortalExtendSystem extends System {

  public PortalExtendSystem() {
    super(PortalExtendComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream()
            .map(this::buildDataObject)
            .forEach(this::applyPortalExtendLogic);
  }

  private PortalExtendSystemData buildDataObject(Entity entity) {
    PortalExtendComponent pec =
        entity
            .fetch(PortalExtendComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(entity, PortalExtendComponent.class));

    TractorBeamComponent tbc =
        entity
            .fetch(TractorBeamComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, TractorBeamComponent.class));

    return new PortalExtendSystemData(entity, pec, tbc);
  }

  private void applyPortalExtendLogic(PortalExtendSystemData data) {
    if (data.pec.isExtended()) {

    } else {
      if (data.pec.throughBlue) {
        // man ist durch das blaue portal gegangen
        PortalFactory.getGreenPortal().ifPresent(portal -> {
          java.lang.System.out.println("Extended blue");
          PositionComponent greenPortalPosition = portal.fetch(PositionComponent.class).get();
          data.pec.onExtend.accept(greenPortalPosition.viewDirection(), greenPortalPosition.position(), data.pec);
          data.pec.isExtended = true;
        });
      } else if (data.pec.throughGreen) {
        // man ist durch das grüne portal gegangen
        PortalFactory.getBluePortal().ifPresent(portal -> {
          java.lang.System.out.println("Extended green");
          PositionComponent bluePortalPosition = portal.fetch(PositionComponent.class).get();
          data.pec.onExtend.accept(bluePortalPosition.viewDirection(), bluePortalPosition.position(), data.pec);
          data.pec.isExtended = true;
        });
      }
    }
  }

  private record PortalExtendSystemData(
      Entity entity, PortalExtendComponent pec, TractorBeamComponent tbc) {}
}
