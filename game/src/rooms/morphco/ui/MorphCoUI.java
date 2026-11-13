package rooms.morphco.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared look of the Morph & Co. control panels: riveted steel plates, signal yellow, conveyor
 * belts, green displays and paper for the documents.
 *
 * <p>Light surfaces always carry black text. Fonts are rasterized for the window's pixel density,
 * so the panels stay sharp on HiDPI screens.
 */
final class MorphCoUI {
  static final Color FRAME = Color.valueOf("1b2025");
  static final Color FLOOR = Color.valueOf("232a30");
  static final Color GROOVE = Color.valueOf("2a3239");
  static final Color STEEL = Color.valueOf("4f5b66");
  static final Color STEEL_DARK = Color.valueOf("323b43");
  static final Color INSET = Color.valueOf("1d2328");
  static final Color TEXT = Color.valueOf("eef2f4");
  static final Color MUTED = Color.valueOf("aeb9c1");
  static final Color INK = Color.BLACK;
  static final Color SIGNAL = Color.valueOf("f7c31d");
  static final Color ORANGE = Color.valueOf("ff9b30");
  static final Color TAG = Color.valueOf("f3e9cb");
  static final Color GHOST = Color.valueOf("8d989f");
  static final Color GO = Color.valueOf("63e283");
  static final Color STOP = Color.valueOf("ff7a68");
  static final Color LED_OFF = Color.valueOf("4a2622");
  static final Color PAPER = Color.valueOf("f4efe0");
  static final Color PAPER_HEADING = Color.valueOf("cdd8e2");
  static final Color PAPER_CELL = Color.valueOf("e8e1cc");

  private static final Color DISPLAY = Color.valueOf("0b1611");
  private static final Color OUTLINE = Color.valueOf("0f1316");
  private static final Color RIVET = Color.valueOf("262e35");
  private static final Color RIVET_SHINE = Color.valueOf("9aa7b1");
  private static final String DOT_FONT = "fonts/Doto_Rounded-ExtraBold.ttf";
  private static final Pattern MARKUP =
      Pattern.compile("\\[n\\]|\\[color=([^\\]]+)\\]|\\[/color\\]|\\[");

  private MorphCoUI() {}

  private static float density() {
    return Math.max(
        (float) Gdx.graphics.getBackBufferWidth() / Math.max(1, Game.windowWidth()),
        (float) Gdx.graphics.getBackBufferHeight() / Math.max(1, Game.windowHeight()));
  }

  static Label label(String text, int size, Color color) {
    return label(text, size, color, Scene2dElementFactory.FONT_PATH);
  }

  static Label bold(String text, int size, Color color) {
    return label(text, size, color, Scene2dElementFactory.FONT_PATH_BOLD);
  }

  /**
   * Dot-matrix lettering for machine displays and the computer screen.
   *
   * @param text displayed text
   * @param size logical font size
   * @param color text color
   * @return wrapped, non-interactive label
   */
  static Label display(String text, int size, Color color) {
    return label(text, size, color, DOT_FONT);
  }

  // Fonts are rasterized for the window's pixel density and scaled back to their logical size.
  private static int raster(int size) {
    return Math.max(1, (int) Math.ceil(size * density()));
  }

  private static Label label(String text, int size, Color color, String path) {
    return setUp(
        Scene2dElementFactory.createLabel(text, FontSpec.of(path, raster(size), color)), size);
  }

  private static Label setUp(Label label, int size) {
    label.setFontScale((float) size / raster(size));
    label.setWrap(true);
    label.setAlignment(Align.topLeft);
    label.setTouchable(Touchable.disabled);
    return label;
  }

  /**
   * Text from the language files with colored passages and forced line breaks.
   *
   * @param text text using the room's {@code [color=...]}, {@code [/color]} and {@code [n]} tags
   * @param size logical font size
   * @param color default text color
   * @return wrapped, non-interactive label
   */
  static Label rich(String text, int size, Color color) {
    BitmapFont font =
        FontHelper.getFont(
            FontSpec.of(Scene2dElementFactory.FONT_PATH, raster(size), Color.WHITE),
            FontHelper.FontRole.MARKUP);
    font.getData().markupEnabled = true;
    // Markup colors are tinted by the label color, so the default color is markup as well.
    return setUp(new Label(markup(text, color), new Label.LabelStyle(font, Color.WHITE)), size);
  }

  /**
   * Translates the room's text tags into libGDX color markup.
   *
   * @param text text with {@code [color=...]}, {@code [/color]} and {@code [n]} tags
   * @param color color of the text outside of color tags
   * @return text for a markup-enabled font; other brackets are escaped
   */
  static String markup(String text, Color color) {
    Matcher matcher = MARKUP.matcher(text);
    StringBuilder result = new StringBuilder("[#" + color + "]");
    while (matcher.find()) {
      String tag = matcher.group();
      String replacement;
      if (tag.equals("[n]")) replacement = "\n";
      else if (tag.equals("[/color]")) replacement = "[]";
      else if (matcher.group(1) != null) replacement = "[" + matcher.group(1).toUpperCase() + "]";
      else replacement = "[[";
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * A push button of the control panel.
   *
   * @param caption localized caption
   * @param primary yellow signal button with black text instead of a steel button
   * @param action called on click
   * @return the button
   */
  static TextButton button(String caption, boolean primary, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(caption, "default", raster(19));
    TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(button.getStyle());
    style.fontColor = primary ? INK : TEXT;
    style.overFontColor = style.fontColor;
    style.downFontColor = style.fontColor;
    Color face = primary ? SIGNAL : STEEL;
    style.up = plate(face);
    style.over = plate(face.cpy().lerp(Color.WHITE, .18f));
    style.down = plate(face.cpy().lerp(Color.BLACK, .2f));
    button.setStyle(style);
    button.getLabel().setFontScale(19f / raster(19));
    button.pad(8, 18, 10, 18);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    return button;
  }

  /**
   * A label tag: black lettering on a light plate, as on the crates and belts of the hall.
   *
   * @param text localized text
   * @return the tag
   */
  static Table tag(String text) {
    Table tag = new Table();
    tag.setBackground(plate(TAG));
    tag.pad(6, 12, 8, 12);
    Label label = bold(text, 17, INK);
    label.setWrap(false);
    tag.add(label).growX();
    return tag;
  }

  static BaseDrawable plate(Color color) {
    return new BaseDrawable() {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        plate(batch, color, batch.getColor().a, x, y, width, height);
      }
    };
  }

  static BaseDrawable fill(Color color) {
    return new BaseDrawable() {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        CanvasGraphics.fill(batch, color, batch.getColor().a, x, y, width, height);
      }
    };
  }

  static BaseDrawable screen() {
    return new BaseDrawable() {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        screen(batch, batch.getColor().a, x, y, width, height);
      }
    };
  }

  // A steel plate with bevelled edges and a rivet in each corner.
  static void plate(
      Batch batch, Color color, float alpha, float x, float y, float width, float height) {
    CanvasGraphics.fill(batch, color, alpha, x, y, width, height);
    Color light = color.cpy().lerp(Color.WHITE, .3f);
    Color dark = color.cpy().lerp(Color.BLACK, .35f);
    CanvasGraphics.fill(batch, light, alpha, x, y + height - 2, width, 2);
    CanvasGraphics.fill(batch, light, alpha, x, y, 1, height);
    CanvasGraphics.fill(batch, dark, alpha, x, y, width, 3);
    CanvasGraphics.fill(batch, dark, alpha, x + width - 1, y, 1, height);
    CanvasGraphics.outline(batch, OUTLINE, alpha, x, y, width, height, 1);
    if (width < 40 || height < 44) return;
    rivet(batch, alpha, x + 5, y + 6);
    rivet(batch, alpha, x + width - 9, y + 6);
    rivet(batch, alpha, x + 5, y + height - 9);
    rivet(batch, alpha, x + width - 9, y + height - 9);
  }

  private static void rivet(Batch batch, float alpha, float x, float y) {
    CanvasGraphics.fill(batch, RIVET, alpha, x, y, 4, 4);
    CanvasGraphics.fill(batch, RIVET_SHINE, alpha, x, y + 2, 2, 2);
  }

  // Yellow and black warning stripes in pixel steps, like the floor markings of the hall.
  static void hazard(Batch batch, float alpha, float x, float y, float width, float height) {
    CanvasGraphics.fill(batch, SIGNAL, alpha, x, y, width, height);
    int rows = Math.max(1, (int) height);
    for (int row = 0; row < rows; row++) {
      for (float start = -rows + row; start < width; start += rows * 2) {
        float left = Math.max(0, start);
        float right = Math.min(width, start + rows);
        if (right > left)
          CanvasGraphics.fill(batch, OUTLINE, alpha, x + left, y + row, right - left, 1);
      }
    }
  }

  // A display with scan lines in a dark bezel.
  static void screen(Batch batch, float alpha, float x, float y, float width, float height) {
    CanvasGraphics.fill(batch, DISPLAY, alpha, x, y, width, height);
    for (float line = y + 2; line < y + height - 2; line += 3)
      CanvasGraphics.fill(batch, GROOVE, alpha * .35f, x + 2, line, width - 4, 1);
    CanvasGraphics.outline(batch, OUTLINE, alpha, x, y, width, height, 2);
  }

  // A conveyor belt: dark slats between two steel rails.
  static void belt(Batch batch, float alpha, float x, float y, float width, float height) {
    CanvasGraphics.fill(batch, INSET, alpha, x, y, width, height);
    for (float slat = x + 12; slat < x + width - 4; slat += 28)
      CanvasGraphics.fill(batch, GROOVE, alpha, slat, y + 6, 2, height - 12);
    plate(batch, STEEL, alpha, x, y, width, 6);
    plate(batch, STEEL, alpha, x, y + height - 6, width, 6);
  }
}
