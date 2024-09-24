package dungine.state.ingame;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.input.MouseScrollEvent;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.graphics.scene.light.SpotLight;
import de.fwatermann.dungine.graphics.scene.model.ModelLoader;
import de.fwatermann.dungine.graphics.simple.Cuboid;
import de.fwatermann.dungine.physics.colliders.BoxCollider;
import de.fwatermann.dungine.physics.ecs.PhysicsDebugSystem;
import de.fwatermann.dungine.physics.ecs.PhysicsSystem;
import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
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


    this.addSystem(this.renderableSystem);
    this.addSystem(this.physicsSystem);
    this.addSystem(this.cameraSystem);
    this.addSystem(this.controlSystem);
    this.addSystem(PhysicsDebugSystem.instance());

    PhysicsDebugSystem.camera(this.camera);
    PhysicsDebugSystem.enable(PhysicsDebugSystem.OPTION_ALL);

    this.player = new Entity();
    this.player.addComponent(new ControlComponent());
    this.player.addComponent(new RenderableComponent(ModelLoader.loadModel(Resource.load("/models/capsule.glb"))));
    CameraComponent cameraComponent = new CameraComponent(this.camera, new Vector3f(0, 5, 2));
    this.player.addComponent(cameraComponent);
    this.player.position().set(0, 2, 0);

    RigidBodyComponent rbc = new RigidBodyComponent();
    rbc.addCollider(new BoxCollider(this.player, new Vector3f(-0.5f, 0, -0.5f), new Vector3f(1, 2, 1)));
    rbc.gravity(false);
    this.player.addComponent(rbc);

    Entity wall = new Entity();
    wall.addComponent(new RenderableComponent(new Cuboid(0xFFFFFFFF)));
    wall.position().set(5, 2, 0);
    wall.size(new Vector3f(1, 5, 5));
    wall.rotation().rotateZ((float) Math.toRadians(-45));
    wall.addComponent(new RigidBodyComponent().addCollider(new BoxCollider(wall, new Vector3f(-0.5f, -2.5f, -2.5f), new Vector3f(1, 5, 5))).gravity(false));
    this.addEntity(wall);


    this.addEntity(this.player);
    this.grid(true);
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
