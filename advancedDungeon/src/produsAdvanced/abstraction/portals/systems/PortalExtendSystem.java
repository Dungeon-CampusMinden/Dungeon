package produsAdvanced.abstraction.portals.systems;

import core.Entity;
import core.System;
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
    filteredEntityStream().map(this::buildDataObject).forEach(this::applyPortalExtendLogic);
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
    boolean refresh = data.pec.needsRefresh;
    if (refresh) {
      // portal wurde gelöscht -> beam löschen
      data.tbc.trim();

      java.lang.System.out.println("TRIMMED THE BEAM");
      // portal wurde neu erstellt -> beam neu erstellen
      if (data.pec.throughBlue) {
        data.pec.throughBlue = false;
        // TODO: Wenn wir das eintritts Portal versetzen, dann darf es nicht als kollisions Portal
        // gelten
        // (wenn es nicht mehr an einer Stelle platziert ist wo der strahl das portal trifft)
        PortalFactory.onBlueCollideEnter(
            PortalFactory.getBluePortal().get(), data.entity, Direction.NONE);
      } else if (data.pec.throughGreen) {
        data.pec.throughGreen = false;
        PortalFactory.onGreenCollideEnter(
            PortalFactory.getGreenPortal().get(), data.entity, Direction.NONE);
      }
      data.pec.needsRefresh = false;
    }
  }

  private record PortalExtendSystemData(
      Entity entity, PortalExtendComponent pec, TractorBeamComponent tbc) {}
}
