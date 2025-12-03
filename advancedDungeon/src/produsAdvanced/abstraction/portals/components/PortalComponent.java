package produsAdvanced.abstraction.portals.components;

import core.Component;
import core.Entity;

/** Holder component for entities are that represents a portal. */
public class PortalComponent implements Component {
  private Entity extendedEntityThrough;

  /**
   * Stores the entity that is being extended for later purposes.
   *
   * @param extendedEntityThrough Entity that is being extended.
   */
  public void setExtendedEntityThrough(Entity extendedEntityThrough) {
    this.extendedEntityThrough = extendedEntityThrough;
  }

  /**
   * Returns the state of extendedEntityThrough.
   *
   * @return Entity that is being extended through a portal.
   */
  public Entity getExtendedEntityThrough() {
    return extendedEntityThrough;
  }
}
