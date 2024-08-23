package test2;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.elements.UIButton;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.utils.BoundingBox2D;
import de.fwatermann.dungine.window.GameWindow;
import org.lwjgl.glfw.GLFW;
import test1.TestState1;

public class UITestState0 extends GameState {

  private long doneAt = 0;

  private UIText title;
  private UIButton buttonStart;
  private UIButton buttonExit;

  public UITestState0(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.title = new UIText(Font.defaultFont(), "Dungine", 60);
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

    this.ui.add(this.title);
    this.ui.add(this.buttonStart);
    this.ui.add(this.buttonExit);

    this.layout();

    this.doneAt = System.currentTimeMillis() + 10_000;
  }

  private void layout() {

    float buttonWidth = (this.window.size().x / 100.0f) * 50.0f;
    float buttonHeight = buttonWidth / 4.0f;

    this.buttonStart.size().set(buttonWidth, buttonHeight, 0.0f);
    this.buttonExit.size().set(buttonWidth, buttonHeight, 0.0f);

    this.buttonStart
        .position()
        .set(
            this.window.size().x / 2.0f - buttonWidth / 2.0f,
            this.window.size().y / 2.0f - buttonHeight / 2.0f,
            0.0f);
    this.buttonExit
        .position()
        .set(
            this.window.size().x / 2.0f - buttonWidth / 2.0f,
            this.window.size().y / 2.0f - buttonHeight / 2.0f - buttonHeight - 10.0f,
            0.0f);

    BoundingBox2D titleBounds =
        this.title.font().calculateBoundingBox(this.title.text(), this.title.fontSize());
    this.title.size().set(titleBounds.width(), titleBounds.height(), 0.0f);
    this.title
        .position()
        .set(
            this.window.size().x / 2.0f - titleBounds.width() / 2.0f,
            this.window.size().y / 2.0f + buttonHeight * 2.0f,
            0.0f);
  }

  @EventHandler
  public void onResize(WindowResizeEvent event) {
    if (event.to.x != event.from.x || event.to.y != event.from.y) {
      this.layout();
    }
  }

  @EventHandler
  public void onKeyboard(KeyboardEvent event) {
    if(event.key == GLFW.GLFW_KEY_ESCAPE && event.action == KeyboardEvent.KeyAction.PRESS) {
      this.window.close();
    }
  }

  @Override
  public boolean loaded() {
    return System.currentTimeMillis() >= this.doneAt;
  }

  @Override
  public float getProgress() {
    return (System.currentTimeMillis() - (this.doneAt - 10_000)) / 10_000.0f;
  }

  @Override
  public void dispose() {}
}
