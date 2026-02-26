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

/** Factory for creating common Scene2d UI elements with consistent styling and behavior. */
public class Scene2dElementFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Scene2dElementFactory.class);
  private static final Skin DEFAULT_SKIN = UIUtils.defaultSkin();

  /** The path to the default font used for UI elements. */
  public static String FONT_PATH = "fonts/Lexend-Regular.ttf";

  /** The path to the bold font used for UI elements. */
  public static String FONT_PATH_BOLD = "fonts/Lexend-Bold.ttf";

  /** The default font specification for UI elements. */
  public static final FontSpec FONT_SPEC = FontSpec.of(FONT_PATH, 24, Color.WHITE);

  /** The default bold font specification for UI elements. */
  public static final FontSpec FONT_SPEC_BOLD = FontSpec.of(FONT_PATH_BOLD, 24, Color.WHITE);

  /**
   * Creates a Label with the specified text and font specification.
   *
   * @param text the text to display in the label
   * @param fontSpec the font specification to use for the label's text
   * @return a new Label instance with the specified text and font specification
   */
  public static Label createLabel(String text, FontSpec fontSpec) {
    Label.LabelStyle style = new Label.LabelStyle();
    style.font = FontHelper.getFont(fontSpec);
    return new Label(text, style);
  }

  /**
   * Creates a Label with the specified text, font size, and color.
   *
   * @param text the text to display in the label
   * @param fontSize the size of the font
   * @param fontColor the color of the font
   * @return a new Label instance
   */
  public static Label createLabel(String text, int fontSize, Color fontColor) {
    return createLabel(text, FontSpec.of(FONT_PATH, fontSize, fontColor));
  }

  /**
   * Creates a Label with the specified text and font size, using the default white color.
   *
   * @param text the text to display in the label
   * @param fontSize the size of the font
   * @return a new Label instance
   */
  public static Label createLabel(String text, int fontSize) {
    return createLabel(text, fontSize, Color.WHITE);
  }

  /**
   * Creates a specialized Button for exiting, pre-configured with the "exit-button" style and
   * cursor.
   *
   * @return a new exit Button instance
   */
  public static Button createExitButton() {
    Button element = new Button(DEFAULT_SKIN, "exit-button");
    Texture tex = ((TextureRegionDrawable) element.getStyle().up).getRegion().getTexture();
    tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    element.setUserObject(Cursors.CROSS);
    return element;
  }

  /**
   * Creates a TextField with default styling and a typing sound effect.
   *
   * @param text the initial text for the field
   * @return a new TextField instance
   */
  public static TextField createTextField(String text) {
    TextField element = new TextField(text, DEFAULT_SKIN);

    TextField.TextFieldStyle style = new TextField.TextFieldStyle(element.getStyle());
    BitmapFont font = FontHelper.getFont(FONT_PATH, 24, Color.WHITE, 0, Color.BLACK);
    style.font = font;
    style.messageFont = font;

    element.setStyle(style);
    element.setUserObject(Cursors.TEXT);
    element.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (actor instanceof TextField) {
              float pitch = 0.85f + (float) Math.random() * 0.3f;
              Sounds.play(CoreSounds.INTERFACE_TEXTFIELD_TYPED, pitch);
            }
          }
        });
    return element;
  }

  /**
   * Adds a ChangeListener to a TextField that executes a Consumer whenever the text changes.
   *
   * @param textField the TextField to listen to
   * @param consumer the action to perform with the new text string
   */
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

  /**
   * Creates a TextButton with a specific style, font size, and hover sounds.
   *
   * @param text the text to display on the button
   * @param styleName the name of the style in the skin
   * @param fontSize the size of the button font
   * @return a new TextButton instance
   */
  public static TextButton createButton(String text, String styleName, int fontSize) {
    TextButton element = new TextButton(text, DEFAULT_SKIN, styleName);
    Label.LabelStyle style = element.getLabel().getStyle();
    style.font = FontHelper.getFont(FONT_PATH_BOLD, fontSize, Color.WHITE, 0, Color.BLACK);
    element.getLabel().setStyle(style);
    element.setUserObject(Cursors.INTERACT);
    addHoverSound(element);
    return element;
  }

  /**
   * Creates a TextButton with a specific style and the default font size.
   *
   * @param text the text to display on the button
   * @param styleName the name of the style in the skin
   * @return a new TextButton instance
   */
  public static TextButton createButton(String text, String styleName) {
    return createButton(text, styleName, 24);
  }

  /**
   * Creates an Image element styled as a horizontal divider.
   *
   * @return a horizontal divider Image
   */
  public static Image createHorizontalDivider() {
    return new Image(DEFAULT_SKIN, "divider");
  }

  /**
   * Creates an Image element styled as a vertical divider.
   *
   * @return a vertical divider Image
   */
  public static Image createVerticalDivider() {
    return new Image(DEFAULT_SKIN, "divider_vertical");
  }

  /**
   * Creates a ScrollPane for the given actor with automatic scroll focus handling.
   *
   * @param actor the content actor to be placed inside the scroll pane
   * @param scrollX whether horizontal scrolling is enabled
   * @param scrollY whether vertical scrolling is enabled
   * @return a new ScrollPane instance
   */
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

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (toActor != null && toActor.isDescendantOf(scrollPane)) return;
            if (event.getStage().getScrollFocus() != scrollPane) return;

            event.getStage().setScrollFocus(null);
          }

          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (event.getStage() == null) return;
            event.getStage().setScrollFocus(scrollPane);
          }
        });
    return scrollPane;
  }

  /**
   * Forces a ScrollPane to scroll to a specific coordinate immediately, then re-enables smooth
   * scrolling.
   *
   * @param scrollPane the ScrollPane to manipulate
   * @param x the target x scroll position
   * @param y the target y scroll position
   */
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

  /**
   * Creates a SelectBox with custom font sizing and a value formatter.
   *
   * @param <T> the type of items in the SelectBox
   * @param valueFormatter a function to convert items to display strings
   * @param small whether to use the small style and font size
   * @return a new SelectBox instance
   */
  public static <T> SelectBox<T> createSelectBox(
      Function<T, String> valueFormatter, boolean small) {
    FontSpec spec = Scene2dElementFactory.FONT_SPEC;
    if (!small) {
      spec = spec.withSize(32);
    }
    BitmapFont font = FontHelper.getFont(spec);
    CustomSelectBox<T> selectBox =
        new CustomSelectBox<>(UIUtils.defaultSkin(), small ? "small" : "default");
    selectBox.getStyle().font = font;
    selectBox.getList().getStyle().font = font;
    selectBox.setValueFormatter(valueFormatter);
    return selectBox;
  }

  /**
   * Creates a small SelectBox with a custom value formatter.
   *
   * @param <T> the type of items in the SelectBox
   * @param valueFormatter a function to convert items to display strings
   * @return a new SelectBox instance
   */
  public static <T> SelectBox<T> createSelectBox(Function<T, String> valueFormatter) {
    return createSelectBox(valueFormatter, true);
  }

  /**
   * Creates a small SelectBox with default formatting.
   *
   * @param <T> the type of items in the SelectBox
   * @return a new SelectBox instance
   */
  public static <T> SelectBox<T> createSelectBox() {
    return createSelectBox(null);
  }

  private static void addHoverSound(Actor actor) {
    actor.addListener(
        new InputListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            // Only play sound if the mouse newly entered this button (not when moving onto a child
            // actor)
            if ((fromActor != null && fromActor.isDescendantOf(actor))
                || pointer != -1
                || (actor instanceof Disableable disableable && disableable.isDisabled())) return;
            Sounds.play(CoreSounds.INTERFACE_ITEM_HOVERED);
            super.enter(event, x, y, pointer, fromActor);
          }
        });
  }
}
