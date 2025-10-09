package produsAdvanced.abstraction.portals.components;

import core.Entity;
import core.Game;
import core.utils.Direction;
import core.utils.Point;
import entities.TractorBeamFactory;
import java.util.List;

public class TractorBeamComponent extends BeamComponent {
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

  @Override
  public void extend(
      Direction direction, Point from, PortalExtendComponent pec, TractorBeamComponent tbc) {
    TractorBeamFactory.extendTractorBeam(direction, from, this.tractorBeamEntities, pec, tbc);
  }

  @Override
  public void trim() {
    TractorBeamFactory.trimAfterFirstBeamEmitter(this.tractorBeamEntities);
  }
}
