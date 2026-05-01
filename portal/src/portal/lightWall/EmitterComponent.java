package portal.lightWall;


import core.Component;

/** Component representing a light wall emitter  */
public class EmitterComponent implements Component {


  private boolean active;

    /**
   * Creates a new emitter for light walls.
   *
   */
  public EmitterComponent() {
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }
}
