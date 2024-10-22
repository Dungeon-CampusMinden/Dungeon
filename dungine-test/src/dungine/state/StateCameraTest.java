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
import dungine.level.OptimizedLevel;
import dungine.systems.CameraSystem;
import org.joml.Math;
import org.joml.Vector3f;

public class StateCameraTest extends GameState {

  private Entity hero, level;

  private Vector3f target;

  public StateCameraTest(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.hero = this.createHero();
    this.level = this.createLevel();
    this.camera.position(0, 5, 5);
    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new CameraSystem(this.camera));
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

    return entity;
  }

  @Override
  public void updateState(float deltaTime) {
    Vector3f toTarget = this.target.sub(this.hero.position(), new Vector3f());
    if(toTarget.length() < 0.1f) {
      this.target.set((float) Math.random() * 32 - 16, 0.5f, (float) Math.random() * 32 - 16);
    } else {
      toTarget.normalize();
      this.hero.position().add(toTarget.mul(0.5f * deltaTime));
    }
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
