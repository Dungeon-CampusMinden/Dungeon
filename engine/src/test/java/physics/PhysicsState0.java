package physics;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.physics.colliders.BoxCollider;
import de.fwatermann.dungine.physics.ecs.PhysicsDebugSystem;
import de.fwatermann.dungine.physics.ecs.PhysicsSystem;
import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class PhysicsState0 extends GameState implements EventListener {

  private static final int DEBUG_FONT_SIZE = 12;

  private boolean loaded = false;

  private CameraPerspective camera;
  private UIText textFps;
  private UIText textEntities;
  private UIText textPhysics;

  private Entity testEntity;

  protected PhysicsState0(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    this.window.vsync(true);
    this.window.rawMouseInput(true);
    this.window.debug(false);

    this.camera =
        new CameraPerspective(new CameraViewport(this.window.size().x, this.window.size().y, 0, 0));

    this.textFps = new UIText(Font.defaultMonoFont(), "FPS: 0", DEBUG_FONT_SIZE);
    this.textEntities = new UIText(Font.defaultMonoFont(), "Entities: c: 0 r: 0", DEBUG_FONT_SIZE);
    this.textPhysics =
        new UIText(Font.defaultMonoFont(), "Physics: t: 0.000000s u: 0", DEBUG_FONT_SIZE);
    this.ui.add(this.textFps);
    this.ui.add(this.textEntities);
    this.ui.add(this.textPhysics);

    this.layout(this.window.size());
    this.enableGrid(this.camera);

    this.addSystem(new FreeCamSystem(this.camera, true, this));
    this.addSystem(new PhysicsSystem());
    this.addSystem(PhysicsDebugSystem.instance());

    PhysicsDebugSystem.camera(this.camera);
    PhysicsDebugSystem.enable(PhysicsDebugSystem.OPTION_ALL);

    {
      Entity entity = new Entity();
      RigidBodyComponent rbc = new RigidBodyComponent().kinematic(true);
      rbc.addCollider(new BoxCollider(entity, new Vector3f(-5, 0, -5), new Vector3f(10, 1, 10)));
      entity.addComponent(rbc);

      this.addEntity(entity);
    }

    this.loaded = true;
    EventManager.getInstance().registerListener(this);
  }

  private void layout(Vector2i size) {
    this.textFps.position().set(10, size.y - (DEBUG_FONT_SIZE + 5) - 5, 0);
    this.textFps.size().set(size.x / 2.0f - 10.0f, 2 * DEBUG_FONT_SIZE, 0);
    this.textEntities.position().set(10, size.y - (2 * (DEBUG_FONT_SIZE + 5)) - 5, 0);
    this.textEntities.size().set(size.x / 2.0f - 10.0f, 2 * DEBUG_FONT_SIZE, 0);
    this.textPhysics.position().set(10, size.y - (3 * (DEBUG_FONT_SIZE + 5)) - 5, 0);
    this.textPhysics.size().set(size.x / 2.0f - 10.0f, 2 * DEBUG_FONT_SIZE, 0);
  }

  @Override
  public boolean loaded() {
    return this.loaded;
  }

  @EventHandler
  private void onResize(WindowResizeEvent event) {
    if (!event.isCanceled()) this.layout(event.to);
  }

  @EventHandler
  private void onKeyboard(KeyboardEvent event) {

    if(event.action == KeyboardEvent.KeyAction.PRESS) {

      if(event.key == GLFW.GLFW_KEY_ESCAPE) {
        this.window.close();
      } else if(event.key == GLFW.GLFW_KEY_R) {
        this.window.setState(new PhysicsState0(this.window));
      } else if(event.key == GLFW.GLFW_KEY_F11) {
        this.window.fullscreen(!this.window.fullscreen());
      } else if(event.key == GLFW.GLFW_KEY_F) {
        //Spawn cube

        Entity entity = new Entity();
        RigidBodyComponent rbc = new RigidBodyComponent();
        rbc.addCollider(new BoxCollider(entity, new Vector3f(), new Vector3f(1)));
        entity.addComponent(rbc);
        entity.position(this.camera.position().add(this.camera.front().mul(2)));
        entity.rotation().rotateX(Math.toRadians(45));
        entity.rotation().rotateZ(Math.toRadians(45));
        this.addEntity(entity);
      }
    }

  }

}
