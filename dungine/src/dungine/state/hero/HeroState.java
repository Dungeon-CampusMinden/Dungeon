package dungine.state.hero;

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
import dungine.state.hero.level.Level;
import org.joml.Vector3f;

public class HeroState extends GameState {

  private Entity hero, level;

  public HeroState(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.hero = this.createHero();
    this.level = this.createLevel();
    this.camera.position(0, 20, 0.001f);
    this.camera.lookAt(0, 0, 0);
    this.addSystem(new RenderableSystem(this.camera));
    //this.addEntity(this.hero);
    this.addEntity(this.level);
  }

  private Entity createLevel() {
    Entity entity = new Entity();
    Level level = new Level();
    entity.position().set(-16, 0, -16);
    entity.addComponent(new RenderableComponent(level));
    return entity;
  }

  private Entity createHero() {
    Entity entity = new Entity();
    //entity.addComponent(new RigidBodyComponent());

    Animation animation = new BatchAnimation(Resource.load("/animations/hero.png"), 4, BatchAnimation.Direction.DOWN)
      .frameDuration(200);
    animation.frameDuration(200);
    Sprite sprite = new Sprite(animation, BillboardMode.CYLINDRICAL);
    entity.addComponent(new RenderableComponent(sprite));

    entity.size(new Vector3f(0.73f, 1.0f, 0.0f));

    return entity;
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
