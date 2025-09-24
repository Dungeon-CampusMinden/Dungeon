package components;

import core.Component;

public class LasergridComponent implements Component {

  private boolean active;

  public LasergridComponent(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  public void toggle() {
    if (this.active) {
      this.deactivate();
    } else {
      this.activate();
    }
  }
}
