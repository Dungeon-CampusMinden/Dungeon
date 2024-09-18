package dungine.state.ingame;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.LightComponent;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.input.MouseScrollEvent;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.graphics.scene.light.AmbientLight;
import de.fwatermann.dungine.graphics.scene.light.SpotLight;
import de.fwatermann.dungine.graphics.scene.model.ModelLoader;
import de.fwatermann.dungine.physics.ecs.PhysicsSystem;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector3f;

public class InGameState extends GameState {

  public InGameState(GameWindow window) {
    super(window);
  }

  private RenderableSystem renderableSystem;
  private PhysicsSystem physicsSystem;
  private FreeCamSystem freeCamSystem;

  private SpotLight spotLight;

  @Override
  public void init() {
    this.renderableSystem = new RenderableSystem(this.camera);
    this.physicsSystem = new PhysicsSystem();
    this.freeCamSystem = new FreeCamSystem(this.camera, true, this);

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

    this.addSystem(this.renderableSystem);
    this.addSystem(this.physicsSystem);
    this.addSystem(this.freeCamSystem);

    Entity mapEntity = new Entity();
    RenderableComponent rc =
        new RenderableComponent(ModelLoader.loadModel(Resource.load("/models/sponza/Sponza.gltf")));
    LightComponent lc = new LightComponent(this.spotLight);
    LightComponent lc2 =
        new LightComponent(new AmbientLight(new Vector3f(0.8f, 0.8f, 1.0f), 0.02f));
    mapEntity.addComponent(rc);
    mapEntity.addComponent(lc);
    //mapEntity.addComponent(lc2);
    mapEntity.size().set(0.01f);
    mapEntity.position().set(100, 0.0f, 100);
    this.addEntity(mapEntity);

    this.camera.position(100, 2, 100);
  }

  @Override
  public void renderState(float deltaTime) {
    this.spotLight.direction(this.camera.front());
    this.spotLight.position(this.camera.position());
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
