package test1;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.Grid3D;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.graphics.simple.CubeColored;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIColorPane;
import de.fwatermann.dungine.ui.elements.UIImage;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

public class Test1State extends GameState implements EventListener {

  private boolean loaded = false;

  private CameraPerspective camera;
  private Grid3D grid;

  public Test1State(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    this.window.vsync(true);
    this.window.rawMouseInput(true);

    UIColorPane colorPane = new UIColorPane(0x00FF00FF);
    colorPane.position(new Vector3f(100, 100, 0)).size(new Vector3f(200, 200, 0));
    this.ui.add(colorPane);

    UIImage image = new UIImage(Resource.load("/textures/tiles/tile_00.png"));
    image.position(new Vector3f(400, 100, 0)).size().set(200, 200, 0);
    this.ui.add(image);

    GL33.glEnable(GL33.GL_BLEND);
    GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
    GL33.glEnable(GL33.GL_DEPTH_TEST);

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

  @Override
  public void renderState(float deltaTime) {
    this.grid.render(this.camera);
  }

  @Override
  public boolean loaded() {
    return this.loaded;
  }

  @Override
  public void dispose() {}

  @EventHandler
  public void onResize(WindowResizeEvent event) {
    this.camera.updateViewport(event.to.x, event.to.y, 0, 0);
  }
}
