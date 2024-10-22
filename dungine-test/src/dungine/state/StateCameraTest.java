package dungine.state;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.simple.Sprite;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.graphics.texture.animation.BatchAnimation;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.elements.UIButton;
import de.fwatermann.dungine.ui.elements.UIColorPane;
import de.fwatermann.dungine.ui.elements.UIImage;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.ui.layout.AlignContent;
import de.fwatermann.dungine.ui.layout.FlexDirection;
import de.fwatermann.dungine.ui.layout.FlexWrap;
import de.fwatermann.dungine.ui.layout.JustifyContent;
import de.fwatermann.dungine.ui.layout.Position;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;
import dungine.components.CameraComponent;
import dungine.level.OptimizedLevel;
import dungine.level.SimpleLevel;
import dungine.systems.CameraSystem;
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
    this.initUI();

    this.hero = this.createHero();
    this.level = this.createLevel();
    this.camera.position(0, 5, 5);
    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new CameraSystem(this.camera));
    this.addEntity(this.level);
    this.addEntity(this.hero);

    this.target = new Vector3f((float) Math.random() * 32 - 16, 0.5f, (float) Math.random() * 32 - 16);
  }

  private void initUI() {
    // Explain box
    UIColorPane box = new UIColorPane(0x3071f2FF, 0xFFFFFFFF, 2, 10);
    box.layout().flow(FlexDirection.COLUMN, FlexWrap.NO_WRAP);
    box.layout().justifyContent(JustifyContent.FLEX_END).alignContent(AlignContent.CENTER);
    box.layout().position(Position.FIXED).bottom(Unit.px(10)).right(Unit.px(10));
    box.layout().width(Unit.px(300)).height(Unit.vH(40));

    UIText text = new UIText(Font.defaultFont(), "In dieser Szene wird das CameraComponent getestet. Es verfolgt den Hero mit einem definierten Offset. Außerdem kann zwischen der einfachen und der optimierten Methode zum rendern der Tiles gewechselt werden (M)", 16, TextAlignment.LEFT);
    text.layout().width(Unit.percent(80));
    box.add(text);

    UIButton back = new UIButton();
    UIImage backImage = new UIImage(Resource.load("/textures/back.png"));
    backImage.layout().width(Unit.percent(80)).height(Unit.percent(80));
    back.add(backImage);
    back.layout().position(Position.FIXED).bottom(Unit.px(10)).left(Unit.px(10));
    back.layout().width(Unit.vH(5)).height(Unit.vH(5));
    back.fillColor(0x3071f2FF).borderRadius(5).borderWidth(0);
    back.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              if (action == MouseButtonEvent.MouseButtonAction.PRESS) {
                this.window.setState(new StateMainMenu(this.window));
              }
            }));

    this.fpsText = new UIText(Font.defaultMonoFont(), "FPS: 0\nSimple", 12, TextAlignment.LEFT);
    this.fpsText.layout().position(Position.FIXED);
    this.fpsText.layout().top(Unit.px(10)).left(Unit.px(10));
    this.fpsText.layout().width(Unit.px(200)).height(Unit.px(12));

    this.ui.add(box);
    this.ui.add(back);
    this.ui.add(this.fpsText);
  }

  private Entity createLevel() {
    Entity entity = new Entity();
    this.levelRenderable = new SimpleLevel();
    entity.position().set(-16, 0.0f, -16);
    entity.addComponent(new RenderableComponent(this.levelRenderable));
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
  public void renderState(float deltaTime) {
    this.fpsText.text("FPS: " + this.window.frameCounter().currentFPS() + "\n" + (this.levelRenderable instanceof SimpleLevel ? "Simple" : "Optimized"));
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

  @EventHandler
  private void onKeyboard(KeyboardEvent event) {
    if(event.action == KeyboardEvent.KeyAction.PRESS) {
      if (event.key == GLFW.GLFW_KEY_M) {
        if(this.levelRenderable instanceof SimpleLevel) {
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
