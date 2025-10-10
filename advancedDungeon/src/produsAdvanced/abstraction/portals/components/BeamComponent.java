package produsAdvanced.abstraction.portals.components;

import core.Component;
import core.utils.Direction;
import core.utils.Point;

public abstract class BeamComponent implements Component {

  public abstract void extend(
      Direction direction, Point from, PortalExtendComponent comp1, TractorBeamComponent comp2);

  public abstract void trim();
}
