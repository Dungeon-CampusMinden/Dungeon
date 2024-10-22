package dungine.state;

import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.elements.UIButton;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.ui.layout.AlignContent;
import de.fwatermann.dungine.ui.layout.FlexDirection;
import de.fwatermann.dungine.ui.layout.FlexWrap;
import de.fwatermann.dungine.ui.layout.JustifyContent;
import de.fwatermann.dungine.ui.layout.UILayouter;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;

public class StateMainMenu extends GameState {

  public StateMainMenu(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    UIButton button1 = new UIButton();
    button1.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.setState(new StateCameraTest(this.window));
            }));
    button1.add(new UIText(Font.defaultFont(), "Camera Test", 24, TextAlignment.CENTER));
    button1.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button1.borderRadius(20).borderWidth(5).fillColor(0x3071f2FF);

    UIButton button2 = new UIButton();
    button2.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.setState(new StatePlayerTest(this.window));
            }));
    button2.add(new UIText(Font.defaultMonoFont(), "Player Test", 24, TextAlignment.CENTER));
    button2.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button2.borderRadius(20).borderWidth(5).fillColor(0x3071f2FF);

    UIButton button3 = new UIButton();
    button3.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.close();
            }));
    button3.add(
        new UIText(Font.defaultMonoFont(), "Exit", 24, TextAlignment.CENTER));
    button3.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button3.borderRadius(20).borderWidth(5).fillColor(0xFF2020FF);

    this.ui.add(button3);
    this.ui.add(button2);
    this.ui.add(button1);

    this.ui
        .layout()
        .flow(FlexDirection.COLUMN, FlexWrap.WRAP)
        .justifyContent(JustifyContent.SPACE_EVENLY)
        .alignContent(AlignContent.CENTER)
        .rowGap(Unit.px(20));
    UILayouter.layout(this.ui, this.window.size(), true);
  }

  @Override
  public void renderState(float deltaTime) {
    int a = 0;
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
