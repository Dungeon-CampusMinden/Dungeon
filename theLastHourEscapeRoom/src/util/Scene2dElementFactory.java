package util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.hud.UIUtils;
import core.utils.FontHelper;
import core.utils.logging.DungeonLogger;

import java.util.function.Consumer;

public class Scene2dElementFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Scene2dElementFactory.class);
  private static final Skin DEFAULT_SKIN = UIUtils.defaultSkin();
  public static String FONT_PATH = "fonts/Lexend-Regular.ttf";
  public static String FONT_PATH_BOLD = "fonts/Lexend-Bold.ttf";

  public static Label createLabel(String text, int fontSize, Color fontColor, int borderSize, Color borderColor){
    Label.LabelStyle style = new Label.LabelStyle();
    style.font = FontHelper.getFont(FONT_PATH, fontSize, fontColor, borderSize, borderColor);
    return new Label(text, style);
  }

  public static Label createLabel(String text, int fontSize, Color fontColor){
    return createLabel(text, fontSize, fontColor, 0, Color.BLACK);
  }

  public static Button createExitButton(){
    Button element = new Button(DEFAULT_SKIN, "exit-button");
    Texture tex = ((TextureRegionDrawable) element.getStyle().up).getRegion().getTexture();
    tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    return element;
  }

  public static TextField createTextField(String text){
    TextField element = new TextField(text, DEFAULT_SKIN);

    TextField.TextFieldStyle style = new TextField.TextFieldStyle(element.getStyle());
    BitmapFont font = FontHelper.getFont(FONT_PATH, 24, Color.WHITE, 0, Color.BLACK);
    style.font = font;
    style.messageFont = font;

    element.setStyle(style);
    return element;
  }

  public static void addTextFieldChangeListener(TextField textField, Consumer<String> consumer){
    textField.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if(actor instanceof TextField textField){
          consumer.accept(textField.getText());
        } else {
          LOGGER.warn("Actor is not a TextField: {}", actor);
        }
      }
    });
  }

  public static TextButton createButton(String text, String styleName, int fontSize) {
    TextButton element = new TextButton(text, DEFAULT_SKIN, styleName);
    Label.LabelStyle style = element.getLabel().getStyle();
    style.font = FontHelper.getFont(FONT_PATH_BOLD, fontSize, Color.WHITE, 0, Color.BLACK);
    element.getLabel().setStyle(style);
    return element;
  }
  public static TextButton createButton(String text, String styleName) {
    return createButton(text, styleName, 24);
  }

  public static Image createHorizontalDivider(){
    return new Image(DEFAULT_SKIN, "divider");
  }

  public static Image createVerticalDivider(){
    return new Image(DEFAULT_SKIN, "divider_vertical");
  }
}
