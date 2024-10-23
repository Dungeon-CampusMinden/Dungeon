package dungine.state;

import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.UIContainer;
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

/**
 * The `StateMainMenu` class represents the main menu state in the game. It is used to display the
 * main menu screen and handle user input for navigating to other game states.
 */
public class StateMainMenu extends GameState {

  public StateMainMenu(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {

    UIContainer<?> demos = new UIContainer<>();
    demos.layout().flow(FlexDirection.COLUMN, FlexWrap.WRAP);
    demos.layout().columnGap(Unit.px(20)).rowGap(Unit.px(20));
    demos.layout().justifyContent(JustifyContent.SPACE_EVENLY).alignContent(AlignContent.CENTER);

    UIButton button1 = new UIButton();
    button1.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.setState(new StateCameraTest(this.window));
            }));
    button1.add(new UIText(Font.defaultFont(), "CameraComponent", 24, TextAlignment.CENTER));
    button1.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button1.borderRadius(20).borderWidth(5).fillColor(0x3071f2FF);

    UIButton button2 = new UIButton();
    button2.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.setState(new StatePlayerTest(this.window));
            }));
    button2.add(new UIText(Font.defaultMonoFont(), "PlayerComponent", 24, TextAlignment.CENTER));
    button2.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button2.borderRadius(20).borderWidth(5).fillColor(0x3071f2FF);

    UIButton button3 = new UIButton();
    button3.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.setState(new StateLighting(this.window));
            }));
    button3.add(new UIText(Font.defaultMonoFont(), "Beleuchtung", 24, TextAlignment.CENTER));
    button3.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button3.borderRadius(20).borderWidth(5).fillColor(0x3071f2FF);

    UIButton button4 = new UIButton();
    button4.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.setState(new StateSolarSystem(this.window));
            }));
    button4.add(new UIText(Font.defaultMonoFont(), "Erde, Mond & Sonne", 24, TextAlignment.CENTER));
    button4.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button4.borderRadius(20).borderWidth(5).fillColor(0x3071f2FF);

    UIButton button5 = new UIButton();
    button5.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.setState(new State3dLevel(this.window));
            }));
    button5.add(new UIText(Font.defaultMonoFont(), "3D-Level", 24, TextAlignment.CENTER));
    button5.layout().width(Unit.vW(33)).height(Unit.vH(20));
    button5.borderRadius(20).borderWidth(5).fillColor(0x3071f2FF);

    UIButton buttonExit = new UIButton();
    buttonExit.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              this.window.close();
            }));
    buttonExit.add(new UIText(Font.defaultMonoFont(), "Exit", 24, TextAlignment.CENTER));
    buttonExit.layout().width(Unit.vW(33)).height(Unit.vH(20));
    buttonExit.borderRadius(20).borderWidth(5).fillColor(0xFF2020FF);

    this.ui.add(buttonExit);
    demos.add(button5);
    demos.add(button4);
    demos.add(button3);
    demos.add(button2);
    demos.add(button1);
    this.ui.add(demos);

    this.ui
        .layout()
        .flow(FlexDirection.COLUMN, FlexWrap.WRAP)
        .justifyContent(JustifyContent.SPACE_EVENLY)
        .alignContent(AlignContent.CENTER)
        .columnGap(Unit.px(20))
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
