package dungine.state;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.simple.Sprite;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.graphics.texture.animation.BatchAnimation;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.window.GameWindow;
import dungine.components.CameraComponent;
import dungine.components.HealthComponent;
import dungine.components.PlayerComponent;
import dungine.level.OptimizedLevel;
import dungine.systems.CameraSystem;
import dungine.systems.HealthSystem;
import dungine.systems.PlayerSystem;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class StatePlayerTest extends GameState {

  private Entity hero, level;

  private Vector3f target;

  public StatePlayerTest(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.hero = this.createHero();
    this.level = this.createLevel();
    this.camera.position(0, 5, 5);
    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new CameraSystem(this.camera));
    this.addSystem(new PlayerSystem());
    this.addSystem(new HealthSystem());

    this.addEntity(this.level);
    this.addEntity(this.hero);

    this.target = new Vector3f((float) Math.random() * 32 - 16, 0.5f, (float) Math.random() * 32 - 16);
  }

  private Entity createLevel() {
    Entity entity = new Entity();
    OptimizedLevel level = new OptimizedLevel();
    entity.position().set(-16, 0.0f, -16);
    entity.addComponent(new RenderableComponent(level));
    return entity;
  }

  private Entity createHero() {
    Entity entity = new Entity();

    Animation animation = new BatchAnimation(Resource.load("/animations/hero.png"), 4, BatchAnimation.Direction.DOWN)
      .frameDuration(200);
    animation.frameDuration(200);
    Sprite sprite = new Sprite(animation, BillboardMode.SPHERICAL);
    entity.addComponent(new RenderableComponent(sprite));
    entity.addComponent(new CameraComponent());
    entity.position().set(0.0f, 0.5f, 0.0f);
    entity.size(new Vector3f(0.73f, 1.0f, 0.0f));

    PlayerComponent playerComponent = new PlayerComponent();
    playerComponent.registerCallback(GLFW.GLFW_KEY_W, (e) -> {
      entity.position().add(0, 0, -2.0f * this.window.renderDeltaTime());
    });
    playerComponent.registerCallback(GLFW.GLFW_KEY_S, (e) -> {
      entity.position().add(0, 0, 2.0f * this.window.renderDeltaTime());
    });
    playerComponent.registerCallback(GLFW.GLFW_KEY_A, (e) -> {
      entity.position().add(-2.0f * this.window.renderDeltaTime(), 0, 0);
    });
    playerComponent.registerCallback(GLFW.GLFW_KEY_D, (e) -> {
      entity.position().add(2.0f * this.window.renderDeltaTime(), 0, 0);
    });
    entity.addComponent(playerComponent);
    entity.addComponent(new HealthComponent());

    return entity;
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
