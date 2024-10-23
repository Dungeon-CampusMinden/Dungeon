package dungine.state;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import dungine.level.OptimizedLevel;
import dungine.level.SimpleLevel;
import dungine.systems.CameraSystem;
import dungine.util.DemoUI;
import dungine.util.HeroFactory;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class StateCameraTest extends GameState {

  private Entity hero, level;

  private Renderable<?> levelRenderable;

  private Vector3f target;

  private UIText fpsText;

  public StateCameraTest(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.fpsText = new UIText(Font.defaultMonoFont(), "FPS: 0", 12, TextAlignment.LEFT);
    DemoUI.init(
        this.window,
        this.ui,
        this.fpsText,
        "In dieser Szene wird das CameraComponent getestet. Die Kamera verfolgt den Hero mit einem definierten Offset. Außerdem kann zwischen der einfachen und der optimierten Methode zum rendern der Tiles gewechselt werden (M)");

    this.hero = HeroFactory.create();
    this.level = this.createLevel();
    this.camera.position(0, 5, 5);
    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new CameraSystem(this.camera));
    this.addEntity(this.level);
    this.addEntity(this.hero);

    this.target =
        new Vector3f((float) Math.random() * 32 - 16, 0.5f, (float) Math.random() * 32 - 16);
  }

  private Entity createLevel() {
    Entity entity = new Entity();
    this.levelRenderable = new SimpleLevel();
    entity.position().set(-16, 0.0f, -16);
    entity.addComponent(new RenderableComponent(this.levelRenderable));
    return entity;
  }

  @Override
  public void renderState(float deltaTime) {
    this.fpsText.text(
        "FPS: "
            + this.window.frameCounter().currentFPS()
            + "\n"
            + (this.levelRenderable instanceof SimpleLevel ? "Simple" : "Optimized"));
  }

  @Override
  public void updateState(float deltaTime) {
    Vector3f toTarget = this.target.sub(this.hero.position(), new Vector3f());
    if (toTarget.length() < 0.1f) {
      this.target.set((float) Math.random() * 32 - 16, 0.5f, (float) Math.random() * 32 - 16);
    } else {
      toTarget.normalize();
      this.hero.position().add(toTarget.mul(0.5f * deltaTime));
    }
  }

  @EventHandler
  private void onKeyboard(KeyboardEvent event) {
    if (event.action == KeyboardEvent.KeyAction.PRESS) {
      if (event.key == GLFW.GLFW_KEY_M) {
        if (this.levelRenderable instanceof SimpleLevel) {
          this.levelRenderable = new OptimizedLevel();
          this.level.component(RenderableComponent.class).ifPresent(this.level::removeComponent);
          this.level.addComponent(new RenderableComponent(this.levelRenderable));
        } else {
          this.levelRenderable = new SimpleLevel();
          this.level.component(RenderableComponent.class).ifPresent(this.level::removeComponent);
          this.level.addComponent(new RenderableComponent(this.levelRenderable));
        }
      }
    }
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
