package core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.hud.UIUtils;
import contrib.hud.elements.CustomSelectBox;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.logging.DungeonLogger;
import java.util.function.Consumer;
import java.util.function.Function;

public class Scene2dElementFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Scene2dElementFactory.class);
  private static final Skin DEFAULT_SKIN = UIUtils.defaultSkin();
  public static String FONT_PATH = "fonts/Lexend-Regular.ttf";
  public static String FONT_PATH_BOLD = "fonts/Lexend-Bold.ttf";
  public static final FontSpec FONT_SPEC = FontSpec.of(FONT_PATH, 24, Color.WHITE);
  public static final FontSpec FONT_SPEC_BOLD = FontSpec.of(FONT_PATH_BOLD, 24, Color.WHITE);

  public static Label createLabel(String text, FontSpec fontSpec) {
    Label.LabelStyle style = new Label.LabelStyle();
    style.font = FontHelper.getFont(fontSpec);
    return new Label(text, style);
  }

  public static Label createLabel(String text, int fontSize, Color fontColor) {
    return createLabel(text, FontSpec.of(FONT_PATH, fontSize, fontColor));
  }

  public static Label createLabel(String text, int fontSize) {
    return createLabel(text, fontSize, Color.WHITE);
  }

  public static Button createExitButton() {
    Button element = new Button(DEFAULT_SKIN, "exit-button");
    Texture tex = ((TextureRegionDrawable) element.getStyle().up).getRegion().getTexture();
    tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    element.setUserObject(Cursors.CROSS);
    return element;
  }

  public static TextField createTextField(String text) {
    TextField element = new TextField(text, DEFAULT_SKIN);

    TextField.TextFieldStyle style = new TextField.TextFieldStyle(element.getStyle());
    BitmapFont font = FontHelper.getFont(FONT_PATH, 24, Color.WHITE, 0, Color.BLACK);
    style.font = font;
    style.messageFont = font;

    element.setStyle(style);
    element.setUserObject(Cursors.TEXT);
    element.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (actor instanceof TextField) {
          float pitch = 0.85f + (float) Math.random() * 0.3f;
          Sounds.playLocal(CoreSounds.INTERFACE_TEXTFIELD_TYPED, pitch);
        }
      }
    });
    return element;
  }

  public static void addTextFieldChangeListener(TextField textField, Consumer<String> consumer) {
    textField.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (actor instanceof TextField textField) {
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
    element.setUserObject(Cursors.INTERACT);
    addHoverSound(element);
    return element;
  }

  public static TextButton createButton(String text, String styleName) {
    return createButton(text, styleName, 24);
  }

  public static Image createHorizontalDivider() {
    return new Image(DEFAULT_SKIN, "divider");
  }

  public static Image createVerticalDivider() {
    return new Image(DEFAULT_SKIN, "divider_vertical");
  }

  public static ScrollPane createScrollPane(Actor actor, boolean scrollX, boolean scrollY) {
    ScrollPane scrollPane = new ScrollPane(actor, DEFAULT_SKIN);
    scrollPane.setScrollingDisabled(!scrollX, !scrollY);
    scrollPane.setFadeScrollBars(true);
    scrollPane.setScrollbarsVisible(true);
    scrollPane.setScrollbarsOnTop(true);
    scrollPane.addListener(
        new InputListener() {
          @Override
          public boolean mouseMoved(InputEvent event, float x, float y) {
            event.getStage().setScrollFocus(scrollPane);
            return false;
          }

          // Prevent scrolling when outside the scroll pane
          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (toActor != null && toActor.isDescendantOf(scrollPane)) return;
            if (event.getStage().getScrollFocus() != scrollPane) return;

            event.getStage().setScrollFocus(null);
          }

          // Rebuilding the UI will cause an exit->enter, so we need to set the scroll focus back
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (event.getStage() == null) return;
            event.getStage().setScrollFocus(scrollPane);
          }
        });
    return scrollPane;
  }

  public static void scrollPaneScrollTo(ScrollPane scrollPane, float x, float y) {
    scrollPane.invalidate();
    scrollPane.layout();
    scrollPane.setSmoothScrolling(false);
    scrollPane.setScrollX(x);
    scrollPane.setScrollY(y);

    if (Gdx.app == null) return;
    Gdx.app.postRunnable(
        () -> {
          scrollPane.setSmoothScrolling(true);
        });
  }

  public static <T> SelectBox<T> createSelectBox(Function<T, String> valueFormatter, boolean small) {
    FontSpec spec = Scene2dElementFactory.FONT_SPEC;
    if(!small){
      spec = spec.withSize(32);
    }
    BitmapFont font = FontHelper.getFont(spec);
    CustomSelectBox<T> selectBox = new CustomSelectBox<>(UIUtils.defaultSkin(), small ? "small" : "default");
    selectBox.getStyle().font = font;
    selectBox.getList().getStyle().font = font;
    selectBox.setValueFormatter(valueFormatter);
    return selectBox;
  }
  public static <T> SelectBox<T> createSelectBox(Function<T, String> valueFormatter) {
    return createSelectBox(valueFormatter, true);
  }
  public static <T> SelectBox<T> createSelectBox() {
    return createSelectBox(null);
  }


  private static void addHoverSound(Actor actor){
    actor.addListener(new InputListener() {
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        // Only play sound if the mouse newly entered this button (not when moving onto a child actor)
        if ((fromActor != null && fromActor.isDescendantOf(actor)) || pointer != -1 || (actor instanceof Disableable disableable && disableable.isDisabled())) return;
        Sounds.playLocal(CoreSounds.INTERFACE_ITEM_HOVERED);
        super.enter(event, x, y, pointer, fromActor);
      }
    });
  }
}
