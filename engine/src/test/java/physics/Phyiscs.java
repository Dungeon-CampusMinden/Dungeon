package physics;

import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;

public class Phyiscs extends GameWindow {

  public Phyiscs() {
    super("Dungine: Testing Physics", new Vector2i(1280, 720), true, false);
  }

  public static void main(String[] args) {
    new Phyiscs().start();
  }

  @Override
  public void init() {
    this.setState(new PhysicsState0(this));
  }

  @Override
  public void cleanup() {

  }
}
