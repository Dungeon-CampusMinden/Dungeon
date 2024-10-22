package dungine;

import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;

public class Dungine extends GameWindow {

  /**
   * Constructs a new GameWindow.
   *
   * @param debug   the debug state of the game window
   */
  public Dungine(boolean debug) {
    super("Dungine", new Vector2i(1280, 720), true, debug);
  }

  @Override
  public void init() {}

  @Override
  public void cleanup() { }
}
