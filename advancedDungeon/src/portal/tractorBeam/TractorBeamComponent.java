package portal.tractorBeam;

import contrib.components.CollideComponent;
import contrib.systems.PositionSync;
import contrib.utils.components.collide.Hitbox;
import core.Component;
import core.Entity;
import core.Game;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import portal.portals.components.PortalExtendComponent;

/** Component represents a tractor beam that can be extended and trimmed. */
public class TractorBeamComponent implements Component {

  private Direction direction;
  private Point from;
  private List<Entity> tractorBeamEntities;
  private boolean active = false;
  private boolean reversed = false;
  private List<Hitbox> oldhitbox = new ArrayList<>();
  private Vector2 forceToApply = Vector2.ZERO;
  private Vector2 reversedForceToApply = Vector2.ZERO;

  /** Store old forces for the Entities. */
  public HashMap<Entity, Vector2> oldForces = new HashMap<>();

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

    activate();
  }

  /** Activates the TractorBeam if not already active. */
  public void activate() {
    if (active) return;
    for (Entity e : tractorBeamEntities) {
      if (Game.allEntities().noneMatch(g -> g.equals(e))) {
        Game.add(e);
      }

      if ("beamEmitter".equals(e.name()) && !oldhitbox.isEmpty()) {
        CollideComponent emitterCC = e.fetch(CollideComponent.class).orElse(null);

        emitterCC.collider(oldhitbox.removeFirst());
        oldhitbox.clear();
        PositionSync.syncPosition(e);
      }
    }
    active = true;
  }

  /** Deactivates the TractorBeam if not already deactivated. */
  public void deactivate() {
    if (!active) return;
    for (Entity e : tractorBeamEntities) {
      boolean isEmitter = "beamEmitter".equals(e.name());
      if (!isEmitter) {
        Game.remove(e);
      } else {
        e.fetch(CollideComponent.class)
            .ifPresent(
                collideComponent -> {
                  oldhitbox.add(
                      new Hitbox(
                          collideComponent.collider().size(),
                          collideComponent.collider().offset()));
                  collideComponent.collider(new Hitbox(0, 0));
                  PositionSync.syncPosition(e);
                });
      }
    }

    active = false;
  }

  /** Toggles the active status of the TractorBeam. */
  public void toggle() {
    if (active) deactivate();
    else activate();
  }

  /** Toggles the reversed status of the TractorBeam. */
  public void toggleReversed() {
    reversed = !reversed;
  }

  /**
   * Shows the current status of the "active" status.
   *
   * @return whether the beam is currently active or not.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Shows the current status of the "reversed" status.
   *
   * @return whether the beam is currently reversed or not.
   */
  public boolean isReversed() {
    return reversed;
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

    if (active) {
      for (Entity e : tractorBeamEntities) {
        if (Game.allEntities().noneMatch(g -> g.equals(e))) {
          Game.add(e);
        }
      }
    }
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

  /**
   * Returns the force that is applied to entities inside the beam when the beam operates in normal
   * mode.
   *
   * @return the force vector applied to entities in the beam
   */
  public Vector2 forceToApply() {
    return forceToApply;
  }

  /**
   * Sets the force that is applied to entities inside the beam when the beam operates in normal
   * mode.
   *
   * @param forceToApply the force vector to apply
   */
  public void forceToApply(Vector2 forceToApply) {
    this.forceToApply = forceToApply;
  }

  /**
   * Returns the force that is applied to entities inside the beam when the beam operates in
   * reversed mode.
   *
   * @return the reversed force vector applied to entities in the beam
   */
  public Vector2 reversedForceToApply() {
    return forceToApply;
  }

  /**
   * Sets the force that is applied to entities inside the beam when the beam operates in reversed
   * mode.
   *
   * @param forceToApply the force vector to apply in reversed mode
   */
  public void reversedForceToApply(Vector2 forceToApply) {
    this.forceToApply = forceToApply;
  }
}
