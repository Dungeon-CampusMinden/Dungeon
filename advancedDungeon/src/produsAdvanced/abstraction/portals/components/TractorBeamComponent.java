package produsAdvanced.abstraction.portals.components;

import core.Component;
import core.Entity;
import core.Game;
import core.utils.Direction;
import core.utils.Point;
import entities.TractorBeamFactory;
import java.util.List;

/** Component represents a tractor beam that can be extended and trimmed. */
public class TractorBeamComponent implements Component {

  private Direction direction;
  private Point from;
  private List<Entity> tractorBeamEntities;

  /**
   * Constructs a TractorBeamComponent so it can be extended and trimmed.
   *
   * @param direction Direction where it extends into.
   * @param from Point from which the extending happens.
   * @param tractorBeamEntities The list of Entities for the Animation.
   */
  public TractorBeamComponent(Direction direction, Point from, List<Entity> tractorBeamEntities) {
    this.direction = direction;
    this.from = from;
    this.tractorBeamEntities = tractorBeamEntities;
    for (Entity tractorBeamEntity : tractorBeamEntities) {
      Game.add(tractorBeamEntity);
    }
  }

  /**
   * Extends the beam from the given point into the given direction.
   *
   * @param direction Direction where it extends into.
   * @param from Point from which the extending happens.
   * @param pec Component for further processing.
   */
  public void extend(Direction direction, Point from, PortalExtendComponent pec) {
    TractorBeamFactory.extendTractorBeam(direction, from, this.tractorBeamEntities, pec, this);
  }

  /** Trims the beam with the internal list. */
  public void trim() {
    TractorBeamFactory.trimAfterFirstBeamEmitter(this.tractorBeamEntities);
  }

  /**
   * Returns the list of all the entities that form the tractor beam.
   *
   * @return the entire list of the entities.
   */
  public List<Entity> getTractorBeamEntities() {
    return tractorBeamEntities;
  }
}
