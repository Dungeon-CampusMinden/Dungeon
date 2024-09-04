package test1;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.components.TextComponent;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.ecs.systems.Render3DTextSystem;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.graphics.simple.CubeColored;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.physics.colliders.AABCollider;
import de.fwatermann.dungine.physics.colliders.BoxCollider;
import de.fwatermann.dungine.physics.ecs.PhysicsDebugComponent;
import de.fwatermann.dungine.physics.ecs.PhysicsDebugSystem;
import de.fwatermann.dungine.physics.ecs.PhysicsSystem;
import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import test2.UITestState0;

public class TestState1 extends GameState implements EventListener {

  private static final Logger LOGGER = LogManager.getLogger(TestState1.class);

  private boolean loaded = false;

  private CameraPerspective camera;
  private UIText fps;
  private UIText debugEntity;
  private UIText debugPhysics;
  private PhysicsSystem physicsSystem;
  private RenderableSystem renderableSystem;

  public TestState1(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    this.window.vsync(false);
    this.window.rawMouseInput(true);
    this.window.debug(false);

    final int fontSize = 12;

    this.fps = new UIText(Font.defaultMonoFont(), "FPS: 0", fontSize);
    this.fps
        .position(new Vector3f(10, this.window.size().y - (1 * (fontSize + 5)), 0))
        .size(new Vector3f(500, 200, 0));
    this.ui.add(this.fps);

    this.debugEntity = new UIText(Font.defaultMonoFont(), "Entities: c: 0 r: 0", fontSize);
    this.debugEntity
        .position(new Vector3f(10, this.window.size().y - (2 * (fontSize + 5)), 0))
        .size(new Vector3f(500, 200, 0));
    this.ui.add(this.debugEntity);

    this.debugPhysics = new UIText(Font.defaultMonoFont(), "Physics: 0s", fontSize);
    this.debugPhysics
        .position(new Vector3f(10, this.window.size().y - (3 * (fontSize + 5)), 0))
        .size(new Vector3f(500, 200, 0));
    this.ui.add(this.debugPhysics);

    this.camera =
        new CameraPerspective(new CameraViewport(this.window.size().x, this.window.size().y, 0, 0));
    this.camera.position(1, 5, 1);
    this.camera.lookAt(0, 0, 0);

    this.renderableSystem = new RenderableSystem(this.camera);

    this.enableGrid(this.camera);
    this.addSystem(new FreeCamSystem(this.camera, true, this));
    this.addSystem(this.renderableSystem);
    this.addSystem((this.physicsSystem = new PhysicsSystem()));
    this.addSystem(new Render3DTextSystem(this.camera));
    this.addSystem(new PhysicsDebugSystem(this.camera));

    {
      Entity entity = new Entity();
      RigidBodyComponent rb = new RigidBodyComponent();
      PhysicsDebugComponent pdc = new PhysicsDebugComponent(true);
      rb.gravity(false).kinematic(true);
      CubeColored cube = new CubeColored(new Vector3f(), 0x606060FF);
      entity.size(new Vector3f(5, 1, 5));
      entity.position().set(0, 0, 0);
      RenderableComponent rc = new RenderableComponent(cube);
      rb.addCollider(
          new BoxCollider(entity, new Vector3f(-2.5f, -0.5f, -2.5f), new Vector3f(5, 1, 5)));
      entity.addComponent(rb).addComponent(rc).addComponent(pdc);
      this.addEntity(entity);
    }

    this.loaded = true;
    EventManager.getInstance().registerListener(this);
  }

  long fpsCountStart = 0;
  long fpsCount = 0;

  @Override
  public void renderState(float deltaTime) {
    if (System.currentTimeMillis() - this.fpsCountStart > 500) {
      this.fps.text("FPS: " + this.fpsCount * 2);
      this.fpsCountStart = System.currentTimeMillis();
      this.fpsCount = 0;
    }
    this.fpsCount++;
    this.debugEntity.text(
        String.format(
            "Entities: c: %d r: %d",
            this.entityCount(), this.renderableSystem.latestRenderCount()));
    this.debugPhysics.text(
        String.format(
            "Physics: T:%.6fs / U:%d",
            this.physicsSystem.lastDeltaTime(), this.physicsSystem.lastUpdates()));
  }

  @Override
  public void updateState(float deltaTime) {}

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
    if (event.action == KeyboardEvent.KeyAction.PRESS) {
      if (event.key == GLFW.GLFW_KEY_G) {
        if (this.renderGrid()) this.disableGrid();
        else this.enableGrid(this.camera);
      } else if (event.key == GLFW.GLFW_KEY_ESCAPE) {
        this.window.setState(new UITestState0(this.window));
      } else if (event.key == GLFW.GLFW_KEY_F11) {
        this.window.fullscreen(!this.window.fullscreen());
      } else if (event.key == GLFW.GLFW_KEY_I) {

        float maxMass = 1000;
        float mass = (float) Math.random() * maxMass;

        // Color between green and red based on mass
        float a = mass / maxMass;
        int r = (int) ((1.0f - a) * 0xFF);
        int g = (int) (a * 0xFF);
        int color = (r << 24) | (g << 16) | 0x00FF;

        Entity e = new Entity();
        RenderableComponent rc = new RenderableComponent(new CubeColored(new Vector3f(), color));
        // SphereCollider sphereCollider = new SphereCollider(e, new Vector3f(0.5f, 0.5f, 0.5f),
        // 0.5f);
        RigidBodyComponent rb = new RigidBodyComponent();
        rb.addCollider(new AABCollider(e, new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));
        e.addComponent(rc).addComponent(rb);
        e.position().set(-0.5f, 5f, -0.5f);
        rb.mass(mass);
        rb.applyForce(
            (float) Math.random() * 5 - 2.5f,
            10.0f,
            (float) Math.random() * 5 - 2.5f,
            RigidBodyComponent.ForceMode.ACCELERATION,
            true);
        rb.onSleep(() -> LOGGER.debug("Entity {} felt asleep", e));
        rb.onWake(() -> LOGGER.debug("Entity {} woke up", e));
        TextComponent tc =
            new TextComponent(new Vector3f(0, 1.5f, 0), "Cube", BillboardMode.SPHERICAL);
        tc.size(new Vector3f(2.0f, 1.0f, 2.0f));
        tc.originMode(TextComponent.OriginMode.CENTER);
        e.addComponent(tc);

        this.addEntity(e);

      } else if (event.key == GLFW.GLFW_KEY_F) {

        Entity e = new Entity();
        RenderableComponent rc =
            new RenderableComponent(new CubeColored(new Vector3f(), 0xFFFF00FF));
        RigidBodyComponent rb = new RigidBodyComponent();
        PhysicsDebugComponent pdc = new PhysicsDebugComponent(true);
        rb.addCollider(new BoxCollider(e, new Vector3f(-0.5f), new Vector3f(1), new Quaternionf()));
        e.addComponent(rc).addComponent(rb).addComponent(pdc);
        e.position().set(this.camera.position().add(this.camera.front().mul(2.0f)));

        if (Keyboard.keyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
          e.rotation().rotateZ(Math.toRadians(45));
          e.rotation().rotateX(Math.toRadians(45));
        }


        // Vector3f force = this.camera.front().mul(10.0f);
        // rb.applyForce(force, RigidBodyComponent.ForceMode.ACCELERATION);

        this.addEntity(e);
      } else if (event.key == GLFW.GLFW_KEY_T) {

        List<Entity> toBeAdded = new ArrayList<>();
        for (int x = -4; x < 5; x++) {
          for (int z = -4; z < 5; z++) {

            Entity entity = new Entity();
            RigidBodyComponent rbc = new RigidBodyComponent();
            RenderableComponent rb =
                new RenderableComponent(new CubeColored(new Vector3f(), 0xFFFF00FF));
            rbc.addCollider(
                new BoxCollider(
                    entity,
                    new Vector3f(-0.25f, -0.25f, -0.25f),
                    new Vector3f(0.5f),
                    new Quaternionf()));
            entity.addComponent(rbc).addComponent(rb);
            entity.position().set(x, 15, z);
            entity.size(new Vector3f(0.5f));
            entity.rotation().rotateX(Math.toRadians(45));
            entity.rotation().rotateY(Math.toRadians(45));
            rbc.mass(1.0f);

            toBeAdded.add(entity);
          }
        }
        this.addEntities(toBeAdded);
      }
    }
  }

  @Override
  public void disposeState() {
    EventManager.getInstance().unregisterListener(this);
  }
}
