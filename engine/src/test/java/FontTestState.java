import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.window.GameWindow;

import java.io.IOException;

public class FontTestState extends GameState {


  /**
   * Create a new game state.
   *
   * @param window the game window
   */
  protected FontTestState(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    try {
      Font font = Font.load(Resource.load("/fonts/segoeui.ttf"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.window.close();
  }

  @Override
  public boolean loaded() {
    return true;
  }

  @Override
  public void dispose() {

  }
}
