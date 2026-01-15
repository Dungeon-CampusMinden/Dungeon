package util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import contrib.hud.UIUtils;
import core.utils.FontHelper;

public class Scene2dElementFactory {

  private static final Skin DEFAULT_SKIN = UIUtils.defaultSkin();

  public static Label createLabel(String text, int fontSize, Color fontColor, int borderSize, Color borderColor){
    Label.LabelStyle style = new Label.LabelStyle();
    style.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, fontSize, fontColor, borderSize, borderColor);
    return new Label(text, style);
  }

  public static Label createLabel(String text, int fontSize, Color fontColor){
    return createLabel(text, fontSize, fontColor, fontSize / 16, Color.BLACK);
  }

  public static TextField createTextField(String text){
    TextField element = new TextField(text, DEFAULT_SKIN);

    TextField.TextFieldStyle style = new TextField.TextFieldStyle(element.getStyle());
    BitmapFont font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, 24, Color.WHITE, 1, Color.BLACK);
    style.font = font;
    style.messageFont = font;

    element.setStyle(style);
    return element;
  }
}
