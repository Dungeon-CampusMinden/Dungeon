package test1;

import de.fwatermann.dungine.audio.AudioBuffer;
import de.fwatermann.dungine.audio.AudioSource;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.AudioSourceComponent;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
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
import de.fwatermann.dungine.graphics.simple.Sprite;
import de.fwatermann.dungine.graphics.simple.Text3D;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.graphics.texture.animation.ArrayAnimation;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.physics.colliders.AABCollider;
import de.fwatermann.dungine.physics.colliders.BoxCollider;
import de.fwatermann.dungine.physics.ecs.PhysicsDebugSystem;
import de.fwatermann.dungine.physics.ecs.PhysicsSystem;
import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import de.fwatermann.dungine.resource.Resource;
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
  private UIText debugRender;
  private PhysicsSystem physicsSystem;
  private RenderableSystem renderableSystem;

  private Animation fireAnimation;


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

    this.debugRender = new UIText(Font.defaultMonoFont(), "Render: f: 0", fontSize);
    this.debugRender.position().set(10, this.window.size().y - (4 * (fontSize + 5)), 0);
    this.debugRender.size().set(500, 200, 0);
    this.ui.add(this.debugRender);

    this.camera =
        new CameraPerspective(new CameraViewport(this.window.size().x, this.window.size().y, 0, 0));
    this.camera.position(1, 5, 1);
    this.camera.lookAt(0, 0, 0);

    this.renderableSystem = new RenderableSystem(this.camera);

    this.enableGrid(this.camera);
    this.addSystem(new FreeCamSystem(this.camera, true, this));
    this.addSystem(this.renderableSystem);
    this.addSystem((this.physicsSystem = new PhysicsSystem()));
    this.addSystem(PhysicsDebugSystem.instance());
    PhysicsDebugSystem.camera(this.camera);

    /*{
      Entity entity = new Entity();
      RigidBodyComponent rb = new RigidBodyComponent();
      rb.gravity(false).kinematic(true);
      CubeColored cube = new CubeColored(new Vector3f(), 0x606060FF);
      entity.size(new Vector3f(5, 1, 5));
      entity.position().set(0, 0, 0);
      RenderableComponent rc = new RenderableComponent(cube);
      rb.addCollider(
          new BoxCollider(entity, new Vector3f(-2.5f, -0.5f, -2.5f), new Vector3f(5, 1, 5)));
      entity.addComponent(rb).addComponent(rc);
      this.addEntity(entity);
    }*/

    {
      Entity entity = new Entity();

      Resource[] frames = new Resource[4];
      for(int i = 0; i < 4; i ++) {
        frames[i] = Resource.load(String.format("/textures/animation/knight/frame_%d.png", i));
      }

      //this.fireAnimation = new BatchAnimation(Resource.load("/textures/animation/animation_3.png"), 32, BatchAnimation.Direction.UP);
      this.fireAnimation = ArrayAnimation.of(frames);
      this.fireAnimation.frameDuration(200);
      this.fireAnimation.blend(false);
      this.fireAnimation.loop(true);

      entity.addComponent(
          new RenderableComponent(
              new Sprite(
                this.fireAnimation,
                  0.57143f,
                  1.0f,
                  BillboardMode.NONE)));
      entity.addComponent(
          new RenderableComponent(new Text3D("Hello World!").offset(new Vector3f(0, 1, 0))));

      entity.size().set(0.57143f, 1.0f, 0.0f);

      AudioBuffer buffer = this.audioContext.createBuffer(Resource.load("/sounds/yes.ogg"), AudioBuffer.AudioFileType.OGGVorbis);
      AudioSourceComponent asc = new AudioSourceComponent(this.audioContext);
      asc.source().setBuffer(buffer).loop(true).play();
      entity.addComponent(asc);

      this.addEntity(entity);
    }

    {
      AudioBuffer buffer = this.audioContext.createBuffer(Resource.load("/sounds/yes.ogg"), AudioBuffer.AudioFileType.OGGVorbis);
      AudioSource source = this.audioContext.createSource("sound", true, false);
      source.position(0, 0, 0);
      source.setBuffer(buffer);
      source.play();

      this.audioContext.camera(this.camera);
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
    this.debugRender.text(String.format("Render: f: %02d", this.fireAnimation.currentFrame()));
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

        this.addEntity(e);

      } else if (event.key == GLFW.GLFW_KEY_F) {

        Entity e = new Entity();
        RenderableComponent rc =
            new RenderableComponent(new CubeColored(new Vector3f(), 0xFFFF00FF));
        RigidBodyComponent rb = new RigidBodyComponent();
        rb.addCollider(new BoxCollider(e, new Vector3f(-0.5f), new Vector3f(1), new Quaternionf()));
        e.addComponent(rc).addComponent(rb);
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
