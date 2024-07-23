import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseMoveEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.mesh.simple.Cube;
import de.fwatermann.dungine.graphics.mesh.simple.CubeColored;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.utils.CoordinateAxis;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

import java.text.NumberFormat;

public class CubeState extends GameState implements EventListener {

  private CameraPerspective camera;
  private CubeColored cube;
  private CoordinateAxis axis;
  private boolean done = false;


  protected CubeState(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.camera = new CameraPerspective();
    this.camera.position(0, 0, 5);
    this.camera.lookAt(0, 0, 0);
    this.cube = new CubeColored(new Vector3f(0, 0, 0), 0xFF0000FF);
    this.axis = new CoordinateAxis(new Vector3f(), 1.0f, true);
    this.done = true;
    EventManager.getInstance().registerListener(this);
  }

  @Override
  public boolean loaded() {
    return this.done;
  }

  @Override
  public void renderState(float deltaTime) {
    this.camera.update();
    this.keyboard(deltaTime);
    if(this.cube != null) this.cube.render(this.camera);
    if(this.axis != null) this.axis.render(this.camera);
  }

  @EventHandler
  private void onResize(WindowResizeEvent event) {
    this.camera.aspectRatio((float) event.to.x / (float) event.to.y);
  }

  @EventHandler
  private void onMouseMove(MouseMoveEvent event) {
    Vector2i rel = event.to.sub(event.from, new Vector2i());
    this.camera.pitchDeg((float) -rel.y);
    this.camera.yawDeg((float) -rel.x);
    if (this.window.hasFocus()) {
      event.to.set(this.window.size().x / 2, this.window.size().y / 2);
    }
  }

  private void keyboard(float deltaTime) {
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_I)) {
      GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_LINE);
    } else {
      GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_FILL);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_K)) {
      GL33.glCullFace(GL33.GL_FRONT);
    } else {
      GL33.glCullFace(GL33.GL_BACK);
    }

    if (Keyboard.keyPressed(GLFW.GLFW_KEY_W)) {
      this.camera.move(
        this.camera.front().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_S)) {
      this.camera.move(
        this.camera.front().mul(-1.0f, 0.0f, -1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_A)) {
      this.camera.move(
        this.camera.right().mul(-1.0f, 0.0f, -1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_D)) {
      this.camera.move(
        this.camera.right().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_SPACE)) {
      this.camera.move(0.0f, deltaTime, 0.0f);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
      this.camera.move(0.0f, -deltaTime, 0.0f);
    }
    this.window.title(this.camera.position().toString(NumberFormat.getInstance()) + " | " + this.camera.front().toString(NumberFormat.getInstance()));
  }

  @Override
  public void dispose() {}
}
