package produsAdvanced.abstraction.portals.components;

import core.Component;
import core.Entity;
import core.Game;
import core.utils.Direction;
import core.utils.Point;
import entities.TractorBeamFactory;
import java.util.List;

public class TractorBeamComponent implements Component {
  private Direction direction;
  private Point from;
  public List<Entity> tractorBeamEntities;

  public TractorBeamComponent(Direction direction, Point from, List<Entity> tractorBeamEntities) {
    this.direction = direction;
    this.from = from;
    this.tractorBeamEntities = tractorBeamEntities;
    for (Entity tractorBeamEntity : tractorBeamEntities) {
      Game.add(tractorBeamEntity);
    }
  }

  public void extend(Direction direction, Point from, PortalExtendComponent pec) {
    TractorBeamFactory.extendTractorBeam(direction, from, this.tractorBeamEntities, pec, this);
  }

  public void trim() {
    TractorBeamFactory.trimAfterFirstBeamEmitter(this.tractorBeamEntities);
  }
}
