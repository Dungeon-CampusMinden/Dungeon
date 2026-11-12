package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import engine.utils.Cursors;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.leveleditor.EditorOverlay;
import feature.leveleditor.PrefabCatalog;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Look of the level editor: colors, fonts and widget styles of its panels, and the colors of the
 * editor overlay in the world.
 *
 * <p>The panels are dark and flat, so they stay calm next to the colorful level. Every prefab
 * category has its own color, used for its markers in the world and its icon in the lists, so a
 * prefab can be recognized in both places. All drawables are generated, nothing depends on the
 * game's skin.
 */
public final class EditorTheme {

  /** Accent of the selected prefab, in the world and in the lists. */
  public static final Color SELECTION = new Color(1f, 0.76f, 0.24f, 1f);

  /** Text drawn on a selection plate. */
  public static final Color ON_SELECTION = new Color(0.12f, 0.09f, 0.02f, 1f);

  /** Accent of interactive elements, e.g. focused fields or active toggles. */
  public static final Color ACCENT = new Color(0.33f, 0.58f, 1f, 1f);

  /** Color of instances that can be clicked to finish a link. */
  public static final Color TARGET = new Color(0.30f, 1f, 0.50f, 1f);

  /** Warnings, e.g. a link that runs for every player. */
  public static final Color WARNING = new Color(1f, 0.66f, 0.2f, 1f);

  /** Errors, e.g. a broken link. */
  public static final Color ERROR = new Color(1f, 0.38f, 0.35f, 1f);

  /** Success messages. */
  public static final Color SUCCESS = new Color(0.45f, 0.88f, 0.5f, 1f);

  /** Primary text on dark backgrounds. */
  public static final Color TEXT = new Color(0.90f, 0.91f, 0.93f, 1f);

  /** Secondary text on dark backgrounds. */
  public static final Color TEXT_MUTED = new Color(0.60f, 0.64f, 0.69f, 1f);

  /** Text of disabled or inactive things on dark backgrounds. */
  public static final Color TEXT_DISABLED = new Color(0.40f, 0.43f, 0.47f, 1f);

  /** Background of panels. */
  public static final Color PANEL = new Color(0.105f, 0.115f, 0.135f, 0.985f);

  /** Background of inputs and wells inside panels. */
  public static final Color FIELD = new Color(0.07f, 0.075f, 0.09f, 1f);

  /** Background of buttons. */
  public static final Color BUTTON = new Color(0.19f, 0.205f, 0.235f, 1f);

  /** Background of hovered buttons. */
  public static final Color BUTTON_OVER = new Color(0.24f, 0.26f, 0.30f, 1f);

  /** Background of pressed buttons. */
  public static final Color BUTTON_DOWN = new Color(0.15f, 0.16f, 0.185f, 1f);

  /** Lines between sections and borders of inputs. */
  public static final Color LINE = new Color(0.25f, 0.27f, 0.31f, 1f);

  /** Background of hovered list rows. */
  public static final Color ROW_OVER = new Color(1f, 1f, 1f, 0.06f);

  /** Background of the selected list row. */
  public static final Color ROW_SELECTED = new Color(1f, 0.76f, 0.24f, 0.17f);

  /** Plate behind labels in the world. */
  public static final Color PLATE = new Color(0.07f, 0.08f, 0.10f, 0.84f);

  /** Label look of the selected instance. */
  public static final EditorOverlay.Look LABEL_SELECTED =
      new EditorOverlay.Look(ON_SELECTION, SELECTION);

  /** Label look of hovered and related instances. */
  public static final EditorOverlay.Look LABEL_FOCUSED = new EditorOverlay.Look(TEXT, PLATE);

  /** Label look of all other instances. */
  public static final EditorOverlay.Look LABEL_NORMAL =
      new EditorOverlay.Look(TEXT_MUTED, new Color(0.07f, 0.08f, 0.10f, 0.7f));

  /** Label look of inactive instances. */
  public static final EditorOverlay.Look LABEL_INACTIVE =
      new EditorOverlay.Look(TEXT_DISABLED, new Color(0.07f, 0.08f, 0.10f, 0.7f));

  /** Label look of warnings. */
  public static final EditorOverlay.Look LABEL_WARNING =
      new EditorOverlay.Look(new Color(0.16f, 0.09f, 0f, 1f), WARNING);

  /** Label look of instances that can be clicked to finish a link. */
  public static final EditorOverlay.Look LABEL_TARGET =
      new EditorOverlay.Look(new Color(0.03f, 0.16f, 0.06f, 1f), TARGET);

  /** Body text size of the panels. */
  public static final int FONT_SIZE = 15;

  /** Size of small texts, e.g. hints and counts. */
  public static final int SMALL_FONT_SIZE = 13;

  /** Height of buttons and inputs. */
  public static final float CONTROL_HEIGHT = 30f;

  /** Height of list rows. */
  public static final float ROW_HEIGHT = 26f;

  private static final String FONT_PATH = "fonts/Roboto-Regular.ttf";
  private static final String BOLD_FONT_PATH = "fonts/Roboto-SemiBold.ttf";
  private static final int RADIUS = 4;
  private static final float SCROLL_BAR_WIDTH = 10f;

  private static final Color OTHER_CATEGORY = new Color(0.62f, 0.66f, 0.71f, 1f);
  private static final Map<String, Color> CATEGORY_COLORS =
      Map.of(
          "Switches", new Color(1f, 0.60f, 0.26f, 1f),
          "Puzzles & Items", new Color(0.36f, 0.85f, 0.45f, 1f),
          "Effects", new Color(0.72f, 0.55f, 1f, 1f),
          "Logic", new Color(0.25f, 0.82f, 0.92f, 1f),
          "Game Flow", new Color(1f, 0.45f, 0.65f, 1f),
          "Visuals", new Color(0.40f, 0.60f, 1f, 1f),
          "Level Setup", new Color(0.62f, 0.66f, 0.71f, 1f));

  private static final Map<String, Drawable> DRAWABLES = new HashMap<>();
  private static TooltipManager tooltips;

  /** Look of a button. */
  public enum Variant {
    /** Regular button. */
    DEFAULT,
    /** Main action of a panel. */
    PRIMARY,
    /** Destructive action, red on hover. */
    DANGER,
    /** Button without background until hovered, e.g. icon buttons in rows. */
    GHOST,
    /** A selected tab or toggle. */
    ACTIVE
  }

  private EditorTheme() {}

  /**
   * Returns the color of a prefab category.
   *
   * @param category category name, see {@link PrefabCatalog}
   * @return the category color, grey for unknown categories
   */
  public static Color categoryColor(String category) {
    return CATEGORY_COLORS.getOrDefault(category, OTHER_CATEGORY);
  }

  /**
   * Returns a copy of a color with another alpha.
   *
   * @param color color
   * @param alpha new alpha
   * @return the copy
   */
  public static Color alpha(Color color, float alpha) {
    return new Color(color.r, color.g, color.b, alpha);
  }

  // ---------------------------------------------------------------------------------------------
  // Fonts and labels

  /**
   * Returns the regular panel font. The glyphs are white, labels color them.
   *
   * @param size font size in pixels
   * @return the font
   */
  public static BitmapFont font(int size) {
    return FontHelper.getFont(FONT_PATH, size, Color.WHITE, 0f, Color.BLACK);
  }

  /**
   * Returns the bold panel font. The glyphs are white, labels color them.
   *
   * @param size font size in pixels
   * @return the font
   */
  public static BitmapFont boldFont(int size) {
    return FontHelper.getFont(BOLD_FONT_PATH, size, Color.WHITE, 0f, Color.BLACK);
  }

  /**
   * Creates a label in the regular font.
   *
   * @param text text
   * @param size font size in pixels
   * @param color text color
   * @return the label
   */
  public static Label label(String text, int size, Color color) {
    return new Label(text, new Label.LabelStyle(font(size), color.cpy()));
  }

  /**
   * Creates a label in the regular font and body size.
   *
   * @param text text
   * @return the label
   */
  public static Label label(String text) {
    return label(text, FONT_SIZE, TEXT);
  }

  /**
   * Creates a label in the bold font.
   *
   * @param text text
   * @param size font size in pixels
   * @param color text color
   * @return the label
   */
  public static Label boldLabel(String text, int size, Color color) {
    return new Label(text, new Label.LabelStyle(boldFont(size), color.cpy()));
  }

  /**
   * Creates the small caption of a panel section, e.g. "EVENTS".
   *
   * @param text caption text
   * @return the label
   */
  public static Label caption(String text) {
    return boldLabel(text.toUpperCase(Locale.ROOT), 12, TEXT_MUTED);
  }

  // ---------------------------------------------------------------------------------------------
  // Drawables

  /**
   * Returns a flat rectangle of a color.
   *
   * @param color color
   * @return the drawable
   */
  public static Drawable flat(Color color) {
    return rounded(color, null, 0);
  }

  /**
   * Returns a rounded rectangle.
   *
   * @param fill fill color
   * @param border border color, or {@code null} for none
   * @param radius corner radius in pixels
   * @return the drawable, stretchable to any size
   */
  public static Drawable rounded(Color fill, Color border, int radius) {
    String key = "r:" + fill + ":" + border + ":" + radius;
    return copy(
        DRAWABLES.computeIfAbsent(key, ignored -> createRounded(fill, border, radius, null)));
  }

  /**
   * Returns a rounded rectangle with a colored bar at its left edge, e.g. for the selected row.
   *
   * @param fill fill color
   * @param bar bar color
   * @return the drawable
   */
  public static Drawable barred(Color fill, Color bar) {
    String key = "b:" + fill + ":" + bar;
    return copy(DRAWABLES.computeIfAbsent(key, ignored -> createRounded(fill, null, RADIUS, bar)));
  }

  // The textures are shared, the sizes are not: callers may change the min size of their copy.
  private static Drawable copy(Drawable drawable) {
    return new NinePatchDrawable((NinePatchDrawable) drawable);
  }

  /**
   * Returns the background of panels.
   *
   * @return the drawable
   */
  public static Drawable panel() {
    return rounded(PANEL, new Color(1f, 1f, 1f, 0.07f), 6);
  }

  /**
   * Returns a drawable with padding, wrapping another drawable.
   *
   * @param drawable drawable to wrap
   * @param horizontal left and right padding
   * @param vertical top and bottom padding
   * @return the padded copy
   */
  public static Drawable padded(Drawable drawable, float horizontal, float vertical) {
    Drawable copy =
        drawable instanceof NinePatchDrawable ninePatch
            ? new NinePatchDrawable(ninePatch)
            : new TextureRegionDrawable((TextureRegionDrawable) drawable);
    copy.setLeftWidth(horizontal);
    copy.setRightWidth(horizontal);
    copy.setTopHeight(vertical);
    copy.setBottomHeight(vertical);
    return copy;
  }

  private static Drawable createRounded(Color fill, Color border, int radius, Color bar) {
    int barWidth = bar == null ? 0 : 3;
    int size = Math.max(2 * radius + 3, barWidth + 2 * radius + 3);
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setBlending(Pixmap.Blending.None);
    pixmap.setColor(0f, 0f, 0f, 0f);
    pixmap.fill();
    if (border != null) {
      fillRounded(pixmap, 0, 0, size, size, radius, border);
      fillRounded(pixmap, 1, 1, size - 2, size - 2, Math.max(0, radius - 1), fill);
    } else {
      fillRounded(pixmap, 0, 0, size, size, radius, fill);
    }
    if (bar != null) {
      pixmap.setColor(bar);
      pixmap.fillRectangle(0, 1, barWidth, size - 2);
    }
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    pixmap.dispose();
    int inset = Math.max(radius, barWidth) + 1;
    NinePatch patch =
        new NinePatch(new TextureRegion(texture), inset, size - inset - 1, radius + 1, radius + 1);
    NinePatchDrawable drawable = new NinePatchDrawable(patch);
    drawable.setLeftWidth(0f);
    drawable.setRightWidth(0f);
    drawable.setTopHeight(0f);
    drawable.setBottomHeight(0f);
    drawable.setMinWidth(0f);
    drawable.setMinHeight(0f);
    return drawable;
  }

  private static void fillRounded(
      Pixmap pixmap, int x, int y, int width, int height, int radius, Color color) {
    pixmap.setColor(color);
    if (radius <= 0) {
      pixmap.fillRectangle(x, y, width, height);
      return;
    }
    pixmap.fillRectangle(x + radius, y, width - 2 * radius, height);
    pixmap.fillRectangle(x, y + radius, width, height - 2 * radius);
    // Quarter circles, drawn pixel by pixel to keep the corners symmetric.
    for (int dy = 0; dy < radius; dy++) {
      for (int dx = 0; dx < radius; dx++) {
        float cx = radius - dx - 0.5f;
        float cy = radius - dy - 0.5f;
        if (cx * cx + cy * cy > radius * radius) continue;
        pixmap.drawPixel(x + dx, y + dy);
        pixmap.drawPixel(x + width - 1 - dx, y + dy);
        pixmap.drawPixel(x + dx, y + height - 1 - dy);
        pixmap.drawPixel(x + width - 1 - dx, y + height - 1 - dy);
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Buttons

  /**
   * Returns the style of text buttons.
   *
   * @param variant look of the button
   * @param size font size
   * @return a new style
   */
  public static TextButton.TextButtonStyle buttonStyle(Variant variant, int size) {
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
    style.font = boldFont(size);
    applyBackground(style, variant);
    style.fontColor = variant == Variant.PRIMARY || variant == Variant.ACTIVE ? Color.WHITE : TEXT;
    style.overFontColor = variant == Variant.DANGER ? Color.WHITE : null;
    style.disabledFontColor = TEXT_DISABLED;
    return style;
  }

  private static void applyBackground(Button.ButtonStyle style, Variant variant) {
    switch (variant) {
      case DEFAULT -> {
        style.up = padded(rounded(BUTTON, null, RADIUS), 10f, 4f);
        style.over = padded(rounded(BUTTON_OVER, null, RADIUS), 10f, 4f);
        style.down = padded(rounded(BUTTON_DOWN, null, RADIUS), 10f, 4f);
      }
      case PRIMARY -> {
        style.up = padded(rounded(new Color(0.20f, 0.45f, 0.88f, 1f), null, RADIUS), 10f, 4f);
        style.over = padded(rounded(new Color(0.26f, 0.52f, 0.94f, 1f), null, RADIUS), 10f, 4f);
        style.down = padded(rounded(new Color(0.17f, 0.39f, 0.78f, 1f), null, RADIUS), 10f, 4f);
      }
      case DANGER -> {
        style.up = padded(rounded(BUTTON, null, RADIUS), 10f, 4f);
        style.over = padded(rounded(new Color(0.62f, 0.2f, 0.19f, 1f), null, RADIUS), 10f, 4f);
        style.down = padded(rounded(new Color(0.5f, 0.15f, 0.14f, 1f), null, RADIUS), 10f, 4f);
      }
      case GHOST -> {
        style.up = padded(rounded(new Color(0f, 0f, 0f, 0f), null, RADIUS), 6f, 3f);
        style.over = padded(rounded(new Color(1f, 1f, 1f, 0.09f), null, RADIUS), 6f, 3f);
        style.down = padded(rounded(new Color(1f, 1f, 1f, 0.04f), null, RADIUS), 6f, 3f);
      }
      case ACTIVE -> {
        Drawable active =
            padded(
                rounded(new Color(0.20f, 0.33f, 0.55f, 1f), alpha(ACCENT, 0.9f), RADIUS), 10f, 4f);
        style.up = active;
        style.over = active;
        style.down = active;
      }
    }
  }

  /**
   * Creates a text button.
   *
   * @param text button text
   * @param variant look of the button
   * @return the button
   */
  public static TextButton button(String text, Variant variant) {
    return button(text, variant, SMALL_FONT_SIZE + 1);
  }

  /**
   * Creates a text button.
   *
   * @param text button text
   * @param variant look of the button
   * @param size font size
   * @return the button
   */
  public static TextButton button(String text, Variant variant, int size) {
    TextButton button = new TextButton(text, buttonStyle(variant, size));
    button.setUserObject(Cursors.INTERACT);
    return button;
  }

  /**
   * Changes the look of a button, e.g. when a toggle is switched.
   *
   * @param button text button
   * @param variant new look
   */
  public static void variant(TextButton button, Variant variant) {
    TextButton.TextButtonStyle style = buttonStyle(variant, SMALL_FONT_SIZE);
    style.font = button.getStyle().font;
    button.setStyle(style);
  }

  /**
   * Creates a square icon button.
   *
   * @param icon icon drawable
   * @param variant look of the button
   * @param tooltip tooltip text, or {@code null}
   * @return the button
   */
  public static ImageButton iconButton(Drawable icon, Variant variant, String tooltip) {
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    applyBackground(style, variant);
    style.up = padded(style.up, 4f, 4f);
    style.over = padded(style.over, 4f, 4f);
    style.down = padded(style.down, 4f, 4f);
    style.imageUp = icon;
    ImageButton button = new ImageButton(style);
    button.getImageCell().size(16f);
    button.getImage().setScaling(Scaling.fit);
    button.setUserObject(Cursors.INTERACT);
    if (tooltip != null) tooltip(button, tooltip);
    return button;
  }

  /**
   * Changes the look of an icon button, e.g. when a toggle is switched.
   *
   * @param button icon button
   * @param variant new look
   */
  public static void variant(ImageButton button, Variant variant) {
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(button.getStyle());
    applyBackground(style, variant);
    style.up = padded(style.up, 4f, 4f);
    style.over = padded(style.over, 4f, 4f);
    style.down = padded(style.down, 4f, 4f);
    button.setStyle(style);
  }

  /**
   * Hides all tooltips, including the ones about to show, e.g. when a button opened a popup that
   * the tooltip of the button would cover.
   */
  public static void hideTooltips() {
    if (tooltips != null) tooltips.hideAll();
  }

  /**
   * Adds a tooltip to an actor.
   *
   * @param actor actor
   * @param text tooltip text
   * @return the tooltip, e.g. to change its text later
   */
  public static TextTooltip tooltip(Actor actor, String text) {
    if (tooltips == null) {
      tooltips = new TooltipManager();
      tooltips.initialTime = 0.45f;
      tooltips.subsequentTime = 0.1f;
      tooltips.resetTime = 0.6f;
      tooltips.animations = false;
    }
    TextTooltip.TextTooltipStyle style = new TextTooltip.TextTooltipStyle();
    style.label = new Label.LabelStyle(font(SMALL_FONT_SIZE), TEXT.cpy());
    style.background =
        padded(rounded(new Color(0.04f, 0.045f, 0.055f, 0.97f), LINE, RADIUS), 8f, 5f);
    style.wrapWidth = 300f;
    TextTooltip tooltip = new TextTooltip(text, tooltips, style);
    actor.addListener(tooltip);
    return tooltip;
  }

  // ---------------------------------------------------------------------------------------------
  // Inputs

  /**
   * Returns the style of text fields.
   *
   * @param size font size
   * @return a new style
   */
  public static TextField.TextFieldStyle textFieldStyle(int size) {
    TextField.TextFieldStyle style = new TextField.TextFieldStyle();
    style.font = font(size);
    style.messageFont = font(size);
    style.fontColor = TEXT.cpy();
    style.messageFontColor = TEXT_DISABLED.cpy();
    style.disabledFontColor = TEXT_DISABLED.cpy();
    style.background = padded(rounded(FIELD, LINE, RADIUS), 8f, 4f);
    style.focusedBackground = padded(rounded(FIELD, ACCENT, RADIUS), 8f, 4f);
    style.disabledBackground = padded(rounded(FIELD, null, RADIUS), 8f, 4f);
    style.cursor = flat(TEXT);
    style.cursor.setMinWidth(1f);
    style.selection = flat(alpha(ACCENT, 0.45f));
    return style;
  }

  /**
   * Creates a text field.
   *
   * @param text initial text
   * @return the text field
   */
  public static TextField textField(String text) {
    return textField(text, FONT_SIZE);
  }

  /**
   * Creates a text field.
   *
   * @param text initial text
   * @param size font size
   * @return the text field
   */
  public static TextField textField(String text, int size) {
    TextField field = Scene2dElementFactory.createTextField(text, size);
    field.setStyle(textFieldStyle(size));
    return field;
  }

  /**
   * Returns the style of select boxes.
   *
   * @param size font size
   * @return a new style
   */
  public static SelectBox.SelectBoxStyle selectBoxStyle(int size) {
    SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
    style.font = font(size);
    style.fontColor = TEXT.cpy();
    style.disabledFontColor = TEXT_DISABLED.cpy();
    style.overFontColor = Color.WHITE.cpy();
    style.background = padded(rounded(BUTTON, null, RADIUS), 8f, 4f);
    style.backgroundOver = padded(rounded(BUTTON_OVER, null, RADIUS), 8f, 4f);
    style.backgroundOpen = padded(rounded(BUTTON_OVER, ACCENT, RADIUS), 8f, 4f);
    style.backgroundDisabled = padded(rounded(BUTTON_DOWN, null, RADIUS), 8f, 4f);
    style.scrollStyle = scrollPaneStyle();
    style.scrollStyle.background =
        padded(rounded(new Color(0.09f, 0.1f, 0.12f, 0.99f), LINE, RADIUS), 2f, 2f);
    style.listStyle = new List.ListStyle();
    style.listStyle.font = font(size);
    style.listStyle.fontColorSelected = Color.WHITE.cpy();
    style.listStyle.fontColorUnselected = TEXT.cpy();
    style.listStyle.selection = padded(rounded(alpha(ACCENT, 0.55f), null, 3), 8f, 3f);
    style.listStyle.over = padded(rounded(ROW_OVER, null, 3), 8f, 3f);
    style.listStyle.background = null;
    return style;
  }

  /**
   * Creates a select box.
   *
   * @param formatter converts values to display text
   * @param <T> value type
   * @return the select box
   */
  public static <T> SelectBox<T> selectBox(Function<T, String> formatter) {
    SelectBox<T> box = Scene2dElementFactory.createSelectBox(formatter);
    box.setStyle(selectBoxStyle(FONT_SIZE));
    return box;
  }

  /**
   * Returns the style of check boxes.
   *
   * @return a new style
   */
  public static CheckBox.CheckBoxStyle checkBoxStyle() {
    CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle();
    style.font = font(FONT_SIZE);
    style.fontColor = TEXT.cpy();
    style.checkboxOff = icon("checkbox-off");
    style.checkboxOn = icon("checkbox-on");
    style.checkboxOver = icon("checkbox-over");
    style.checkboxOnOver = icon("checkbox-on");
    return style;
  }

  /**
   * Creates a check box without text.
   *
   * @return the check box
   */
  public static CheckBox checkBox() {
    CheckBox box = new CheckBox("", checkBoxStyle());
    box.getImageCell().size(18f);
    box.setUserObject(Cursors.INTERACT);
    return box;
  }

  /**
   * Returns the style of sliders.
   *
   * @return a new style
   */
  public static Slider.SliderStyle sliderStyle() {
    Slider.SliderStyle style = new Slider.SliderStyle();
    Drawable track = rounded(LINE, null, 1);
    track.setMinHeight(4f);
    style.background = track;
    Drawable before = rounded(ACCENT, null, 1);
    before.setMinHeight(4f);
    style.knobBefore = before;
    style.knob = icon("knob");
    style.knobOver = icon("knob-over");
    style.knobDown = icon("knob-over");
    return style;
  }

  /**
   * Returns the style of scroll panes: a thin knob, no background.
   *
   * @return a new style
   */
  public static ScrollPane.ScrollPaneStyle scrollPaneStyle() {
    ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
    Drawable knob = rounded(new Color(1f, 1f, 1f, 0.22f), null, 3);
    knob.setMinWidth(SCROLL_BAR_WIDTH);
    knob.setMinHeight(24f);
    style.vScrollKnob = knob;
    Drawable horizontalKnob = rounded(new Color(1f, 1f, 1f, 0.22f), null, 3);
    horizontalKnob.setMinHeight(SCROLL_BAR_WIDTH);
    horizontalKnob.setMinWidth(24f);
    style.hScrollKnob = horizontalKnob;
    return style;
  }

  /**
   * Creates a vertical scroll pane in the editor style.
   *
   * @param content scrolled actor
   * @return the scroll pane
   */
  public static ScrollPane scrollPane(Actor content) {
    ScrollPane pane = Scene2dElementFactory.createScrollPane(content, false, true);
    pane.setStyle(scrollPaneStyle());
    pane.setFlickScroll(false);
    pane.setFadeScrollBars(false);
    // The bar gets its own column, so it never covers the end of a row.
    pane.setScrollbarsOnTop(false);
    pane.setOverscroll(false, false);
    return pane;
  }

  /**
   * Creates a thin horizontal line between sections.
   *
   * @return the line
   */
  public static Image divider() {
    Image line = new Image(flat(LINE));
    line.setScaling(Scaling.stretchX);
    return line;
  }

  // ---------------------------------------------------------------------------------------------
  // Icons

  /**
   * Returns an image of the game's assets as a drawable, e.g. one of the HUD icons.
   *
   * @param path asset path
   * @return the drawable
   */
  public static Drawable asset(String path) {
    return DRAWABLES.computeIfAbsent(
        "a:" + path,
        ignored ->
            new TextureRegionDrawable(
                new TextureRegion(TextureMap.instance().textureAt(new SimpleIPath(path)))));
  }

  /**
   * Returns one of the editor's own icons, drawn white on transparent so it can be tinted.
   *
   * @param name icon name, see {@link EditorIcons}
   * @return the drawable
   */
  public static Drawable icon(String name) {
    return DRAWABLES.computeIfAbsent("i:" + name, ignored -> EditorIcons.create(name));
  }

  /**
   * Returns one of the editor's own icons tinted in a color.
   *
   * @param name icon name, see {@link EditorIcons}
   * @param color tint
   * @return the drawable
   */
  public static Drawable icon(String name, Color color) {
    String key = "i:" + name + ":" + color;
    Drawable cached = DRAWABLES.get(key);
    if (cached != null) return cached;
    Drawable tinted = ((TextureRegionDrawable) icon(name)).tint(color);
    DRAWABLES.put(key, tinted);
    return tinted;
  }
}
