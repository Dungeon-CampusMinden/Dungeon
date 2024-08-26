package test1;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.Grid3D;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.graphics.simple.CubeColored;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import test2.UITestState0;

public class TestState1 extends GameState implements EventListener {

  private boolean loaded = false;

  private CameraPerspective camera;
  private Grid3D grid;
  private UIText fps;

  public TestState1(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    this.window.vsync(false);
    this.window.rawMouseInput(true);
    this.window.debug(false);

//    UIColorPane colorPane = new UIColorPane(0x00FF00FF);
//    colorPane.position(new Vector3f(100, 100, 0)).size(new Vector3f(200, 200, 0));
//    this.ui.add(colorPane);
//
//    UIImage image = new UIImage(Resource.load("/textures/tiles/tile_00.png"));
//    image.position(new Vector3f(400, 100, 0)).size().set(200, 200, 0);
//    this.ui.add(image);

    this.fps = new UIText(Font.defaultMonoFont(), "Hello, World!");
    this.fps.position(new Vector3f(10, 10, 0)).size(new Vector3f(200, 200, 0));
    this.ui.add(this.fps);

    this.camera = new CameraPerspective(new CameraViewport(this.window.size().x, this.window.size().y, 0, 0));
    this.camera.position(1, 5, 1);
    this.camera.lookAt(0, 0, 0);

    this.grid = new Grid3D();

    Entity entity = new Entity();
    entity.addComponent(new RenderableComponent(new CubeColored(new Vector3f(), 0xFF0000FF)));

    this.addEntity(entity);
    this.addSystem(new FreeCamSystem(this.camera, true, this));
    this.addSystem(new RenderableSystem(this.camera));

    this.loaded = true;
    EventManager.getInstance().registerListener(this);
  }

  long fpsCountStart = 0;
  long fpsCount = 0;

  @Override
  public void renderState(float deltaTime) {
    this.grid.render(this.camera);

    if(System.currentTimeMillis() - this.fpsCountStart > 500) {
      this.fps.text("FPS: " + this.fpsCount * 2);
      this.fpsCountStart = System.currentTimeMillis();
      this.fpsCount = 0;
    }
    this.fpsCount ++;
  }

  @Override
  public boolean loaded() {
    return this.loaded;
  }

  @EventHandler
  public void onResize(WindowResizeEvent event) {
    this.camera.updateViewport(event.to.x, event.to.y, 0, 0);
  }

  @EventHandler
  public void onKeyboard(KeyboardEvent event) {
    if(event.key == GLFW.GLFW_KEY_ESCAPE && event.action == KeyboardEvent.KeyAction.PRESS) {
      this.window.setState(new UITestState0(this.window));
    }
  }

  @Override
  public void dispose() {
    EventManager.getInstance().unregisterListener(this);
  }

}