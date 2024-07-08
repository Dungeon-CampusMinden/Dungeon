import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.GameStateTransition;
import de.fwatermann.dungine.window.GameWindow;
import org.lwjgl.opengl.GL33;

public class LoadingScreenTransition extends GameStateTransition {

  public LoadingScreenTransition(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

  }

  @Override
  public void render(float deltaTime, GameState to) {
    GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
  }

  @Override
  public void dispose() {

  }
}
