package dungine.state.ingame;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.LightComponent;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.input.MouseScrollEvent;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.graphics.scene.light.DirectionalLight;
import de.fwatermann.dungine.graphics.scene.light.SpotLight;
import de.fwatermann.dungine.graphics.scene.model.ModelLoader;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.physics.ecs.PhysicsSystem;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import dungine.state.ingame.components.CameraComponent;
import dungine.state.ingame.components.ControlComponent;
import dungine.state.ingame.systems.CameraSystem;
import dungine.state.ingame.systems.ControlSystem;
import org.joml.Vector3f;

public class InGameState extends GameState {

  public InGameState(GameWindow window) {
    super(window);
  }

  private RenderableSystem renderableSystem;
  private PhysicsSystem physicsSystem;
  private CameraSystem cameraSystem;
  private ControlSystem controlSystem;

  private SpotLight spotLight;

  private Entity player;

  private UIText textCameraPos;

  @Override
  public void init() {
    this.renderableSystem = new RenderableSystem(this.camera);
    this.physicsSystem = new PhysicsSystem();
    this.cameraSystem = new CameraSystem();
    this.controlSystem = new ControlSystem();

    this.skyBox = new SkyBox(Resource.load("/images/skybox.png"));
    this.spotLight =
        new SpotLight(
            new Vector3f(0),
            new Vector3f(1.0f),
            new Vector3f(1.0f),
            1.0f,
            1.0f,
            0.1f,
            0.8f,
            0.9f,
            0.1f);

    this.textCameraPos = new UIText(Font.defaultMonoFont(), "Camera: ", 12);
    this.textCameraPos.position().set(20, 20, 0);
    this.textCameraPos.size().set(500, 20, 0);
    this.ui.add(this.textCameraPos);

    this.addSystem(this.renderableSystem);
    this.addSystem(this.physicsSystem);
    this.addSystem(this.cameraSystem);
    this.addSystem(this.controlSystem);

    this.player = new Entity();
    this.player.addComponent(new ControlComponent());
    this.player.addComponent(new RenderableComponent(ModelLoader.loadModel(Resource.load("/models/capsule.glb"))));
    CameraComponent cameraComponent = new CameraComponent(this.camera, new Vector3f(0, 5, 2));
    this.player.addComponent(cameraComponent);
    this.player.position().set(0, 2, 0);

    this.addEntity(this.player);

    this.grid(true);

    Entity mapEntity = new Entity();
    LightComponent lc = new LightComponent(this.spotLight);
    LightComponent lc2 =
        new LightComponent(
            new DirectionalLight(
                new Vector3f(1.0f, -1.0f, -1.0f).normalize(),
                new Vector3f(1.0f),
                0.5f));
    mapEntity.addComponent(new RenderableComponent(ModelLoader.loadModel(Resource.load("/models/earth.glb"))));
    mapEntity.addComponent(lc);
    mapEntity.addComponent(lc2);
    mapEntity.size().set(100, 100, 100);
    mapEntity.position().set(0, -100, 0);
    this.addEntity(mapEntity);

  }

  @Override
  public void renderState(float deltaTime) {
    this.spotLight.direction(this.camera.front());
    this.spotLight.position(this.camera.position());

    Vector3f camPos = this.camera.position();
    this.textCameraPos.text(String.format("Camera: x:%.3f y:%.3f z:%.3f", camPos.x, camPos.y, camPos.z));
  }

  @EventHandler
  public void onKeyboard(KeyboardEvent event) {
    if (event.action == KeyboardEvent.KeyAction.PRESS) {
      if (event.key == 300) {
        this.window.fullscreen(!this.window.fullscreen());
      } else if (event.key == 256) {
        this.window.close();
      }
    }
  }

  @EventHandler
  public void onScroll(MouseScrollEvent event) {
    this.spotLight.cutOff(this.spotLight.cutOff() + event.y * 0.02f);
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
