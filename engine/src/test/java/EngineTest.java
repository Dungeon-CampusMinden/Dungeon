import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import test1.Test1State;

public class EngineTest extends GameWindow implements EventListener {

  public static void main(String[] args) {
    new EngineTest("Game", new Vector2i(1280, 720), true, true).start();
  }

  public EngineTest(String title, Vector2i size, boolean visible, boolean debug) {
    super(title, size, visible, debug);
  }

  @Override
  public void init() {
    this.vsync(true);
    EventManager.getInstance().registerListener(this);
    // this.setState(new CubeState(this));
    this.setState(new Test1State(this));
    //this.setState(new ChunkTest(this));
  }

  @Override
  public void cleanup() {}

  @EventHandler
  public void onKey(KeyboardEvent event) {
    if (event.action == KeyboardEvent.KeyAction.PRESS && event.key == GLFW.GLFW_KEY_F11) {
      this.fullscreen(!this.fullscreen());
    }
    if (event.action == KeyboardEvent.KeyAction.PRESS && event.key == GLFW.GLFW_KEY_ESCAPE) {
      this.close();
    }
  }
}
