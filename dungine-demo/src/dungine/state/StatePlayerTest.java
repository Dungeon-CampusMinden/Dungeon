package dungine.state;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import dungine.level.OptimizedLevel;
import dungine.systems.CameraSystem;
import dungine.systems.HealthSystem;
import dungine.systems.PlayerSystem;
import dungine.systems.VelocitySystem;
import dungine.util.DemoUI;
import dungine.util.HeroFactory;

public class StatePlayerTest extends GameState {

  private Entity hero, level;
  private UIText fpsText;

  public StatePlayerTest(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    this.fpsText = new UIText(Font.defaultMonoFont(), "FPS: 0", 12, TextAlignment.LEFT);
    DemoUI.init(this.window, this.ui, this.fpsText, "In dieser Szene wird das PlayerComponent getestet. Mit 'W','A','S','D' kann der Hero durch das Level bewegt werden.");

    this.hero = HeroFactory.create();
    HeroFactory.makeControlled(this.window, this.hero);

    this.level = this.createLevel();
    this.camera.position(0, 5, 5);
    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new CameraSystem(this.camera));
    this.addSystem(new PlayerSystem());
    this.addSystem(new HealthSystem());
    this.addSystem(new VelocitySystem());

    this.addEntity(this.level);
    this.addEntity(this.hero);
  }

  private Entity createLevel() {
    Entity entity = new Entity();
    OptimizedLevel level = new OptimizedLevel();
    entity.position().set(-16, 0.0f, -16);
    entity.addComponent(new RenderableComponent(level));
    return entity;
  }

  @Override
  public void renderState(float deltaTime) {
    this.fpsText.text("FPS: " + this.window.frameCounter().currentFPS());
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
