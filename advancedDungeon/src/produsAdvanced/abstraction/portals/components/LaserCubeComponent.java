package produsAdvanced.abstraction.portals.components;

import core.Component;

public class LaserCubeComponent implements Component {

  private boolean active = false;


  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
