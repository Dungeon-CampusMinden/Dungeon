package dungine.util.health;

import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.ui.UIRoot;
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
import dungine.state.StateMainMenu;

public class DemoUI {

  public static void init(GameWindow window, UIRoot root, UIText fpsText, String description) {
    UIColorPane box = new UIColorPane(0x3071f280, 0xFFFFFFFF, 2, 10);
    box.layout().flow(FlexDirection.COLUMN, FlexWrap.NO_WRAP);
    box.layout().justifyContent(JustifyContent.CENTER).alignContent(AlignContent.CENTER);
    box.layout().position(Position.FIXED).bottom(Unit.px(10)).right(Unit.px(10));
    box.layout().width(Unit.px(300)).height(Unit.vH(40));

    UIText text = new UIText(Font.defaultFont(), description, 16, TextAlignment.LEFT);
    text.layout().width(Unit.percent(80)).height(Unit.percent(80));
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
            window.setState(new StateMainMenu(window));
          }
        }));

    fpsText.layout().position(Position.FIXED);
    fpsText.layout().top(Unit.px(10)).left(Unit.px(10));
    fpsText.layout().width(Unit.px(200));

    root.add(box);
    root.add(back);
    root.add(fpsText);
  }

}
