package test2;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.elements.UIButton;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.utils.BoundingBox2D;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import test1.TestState1;

public class UITestState0 extends GameState implements EventListener {

  private static final long LOAD_DURATION = 1000;

  private long doneAt = 0;

  private UIText title;
  private UIButton buttonStart;
  private UIButton buttonExit;

  private UIText textStart;
  private UIText textEnd;

  public UITestState0(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    this.title = new UIText(Font.defaultMonoFont(), "Dungine", 60);
    this.buttonStart = new UIButton();
    this.buttonExit = new UIButton();

    this.buttonStart.attachComponent(
        new UIComponentClickable(
            (e) -> {
              this.window.setState(new TestState1(this.window));

            }));
    this.buttonExit.attachComponent(
        new UIComponentClickable(
            (e) -> {
              this.window.close();
            }));
    this.textStart = new UIText(Font.defaultMonoFont(), "Start", 40);
    this.buttonStart.add(this.textStart);

    this.textEnd = new UIText(Font.defaultMonoFont(), "Exit", 40);
    this.buttonExit.add(this.textEnd);

    this.ui.add(this.title);
    this.ui.add(this.buttonStart);
    this.ui.add(this.buttonExit);

    this.layout(this.window.size());

    this.doneAt = System.currentTimeMillis() + LOAD_DURATION;

    EventManager.getInstance().registerListener(this);
  }

  private void layout(Vector2i size) {

    float buttonWidth = (size.x / 100.0f) * 50.0f;
    float buttonHeight = buttonWidth / 4.0f;

    this.buttonStart.size().set(buttonWidth, buttonHeight, 0.0f);
    this.buttonExit.size().set(buttonWidth, buttonHeight, 0.0f);

    this.buttonStart
        .position()
        .set(size.x / 2.0f - buttonWidth / 2.0f, size.y / 2.0f - buttonHeight / 2.0f, 0.0f);
    this.buttonExit
        .position()
        .set(
            size.x / 2.0f - buttonWidth / 2.0f,
            size.y / 2.0f - buttonHeight / 2.0f - buttonHeight - 10.0f,
            0.0f);

    BoundingBox2D startTextBounds = this.textStart.font().calculateBoundingBox(this.textStart.text(), this.textStart.fontSize(), Math.round(this.buttonStart.size().x));
    this.textStart.size().set(startTextBounds.width(), startTextBounds.height(), 0.0f);
    this.textStart.position().set(this.buttonStart.size().x / 2 - startTextBounds.width() / 2.0f, this.buttonStart.size().y / 2 - startTextBounds.height() / 2.0f, 1.0f);

    BoundingBox2D endTextBounds = this.textEnd.font().calculateBoundingBox(this.textEnd.text(), this.textEnd.fontSize(), Math.round(this.buttonExit.size().x));
    this.textEnd.size().set(endTextBounds.width(), endTextBounds.height(), 0.0f);
    this.textEnd.position().set(this.buttonExit.size().x / 2 - endTextBounds.width() / 2.0f, this.buttonExit.size().y / 2 - endTextBounds.height() / 2.0f, 1.0f);

    BoundingBox2D titleBounds =
        this.title.font().calculateBoundingBox(this.title.text(), this.title.fontSize());
    this.title.size().set(titleBounds.width(), titleBounds.height(), 0.0f);
    float x = size.x / 2.0f - titleBounds.width() / 2.0f;
    float y = size.y - (this.buttonStart.position().y + this.buttonStart.size().y) / 2.0f + titleBounds.height() / 2.0f;
    this.title
        .position()
        .set(x, y, 0.0f);
  }

  @EventHandler
  public void onResize(WindowResizeEvent event) {
    if (event.to.x != event.from.x || event.to.y != event.from.y) {
      this.layout(event.to);
    }
  }

  @EventHandler
  public void onKeyboard(KeyboardEvent event) {
    if (event.key == GLFW.GLFW_KEY_ESCAPE && event.action == KeyboardEvent.KeyAction.PRESS) {
      this.window.close();
    }
  }

  @Override
  public boolean loaded() {
    return System.currentTimeMillis() >= this.doneAt;
  }

  @Override
  public float getProgress() {
    return (System.currentTimeMillis() - (this.doneAt - LOAD_DURATION)) / (float) LOAD_DURATION;
  }

  @Override
  public void disposeState() {
    EventManager.getInstance().unregisterListener(this);
  }
}
