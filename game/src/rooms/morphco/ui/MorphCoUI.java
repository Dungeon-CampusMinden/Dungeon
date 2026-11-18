package rooms.morphco.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.hud.elements.RichLabel;

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
  static final Color OUTLINE = Color.valueOf("0f1316");
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
  static final Color LAMP_OFF = Color.valueOf("26332b");
  static final Color PAPER = Color.valueOf("f4efe0");
  static final Color PAPER_HEADING = Color.valueOf("cdd8e2");
  static final Color PAPER_CELL = Color.valueOf("e8e1cc");
  static final Color CRATE = Color.valueOf("455b7c");
  static final Color CRATE_DARK = Color.valueOf("222c3b");

  private static final Color DISPLAY = Color.valueOf("0b1611");
  private static final Color RIVET = Color.valueOf("262e35");
  private static final Color RIVET_SHINE = Color.valueOf("9aa7b1");
  private static final Color WOOD = Color.valueOf("7a5234");
  private static final Color WOOD_GRAIN = Color.valueOf("6a4529");
  private static final Color WOOD_EDGE = Color.valueOf("553620");
  private static final Color BOWL_RIM = Color.valueOf("c3ccd3");
  private static final Color BOWL_BODY = Color.valueOf("7c8a95");
  private static final Color BOWL_DEEP = Color.valueOf("262d33");
  private static final Color BOWL_FLOOR = Color.valueOf("46515a");
  private static final String DOT_FONT = "fonts/Doto_Rounded-ExtraBold.ttf";

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
   * @param text text using {@code [color=...]}, {@code [/color]} and {@code [n]} tags
   * @param size logical font size
   * @param color default text color
   * @return wrapped, non-interactive label
   */
  static RichLabel rich(String text, int size, Color color) {
    RichLabel label =
        new RichLabel(text, FontSpec.of(Scene2dElementFactory.FONT_PATH, size, color));
    // Like a wrapped Label: the cell decides the width, the text wraps inside it.
    label.setMaxPrefWidth(1);
    label.setTouchable(Touchable.disabled);
    return label;
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
    plates(style, primary ? SIGNAL : STEEL);
    button.setStyle(style);
    button.getLabel().setFontScale(19f / raster(19));
    button.pad(8, 18, 10, 18);
    onChange(button, action);
    return button;
  }

  /**
   * A push button of the control panel without a caption, e.g. for an icon.
   *
   * @param face color of the button
   * @param action called on click
   * @return the empty button
   */
  static Button plateButton(Color face, Runnable action) {
    Button.ButtonStyle style = new Button.ButtonStyle();
    plates(style, face);
    Button button = new Button(style);
    onChange(button, action);
    return button;
  }

  // The plate of a button, lighter under the pointer and darker while pressed.
  private static void plates(Button.ButtonStyle style, Color face) {
    style.up = plate(face);
    style.over = plate(face.cpy().lerp(Color.WHITE, .18f));
    style.down = plate(face.cpy().lerp(Color.BLACK, .2f));
  }

  private static void onChange(Button button, Runnable action) {
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
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

  /**
   * Measures the height a table needs at a given width.
   *
   * @param table table with wrapped labels
   * @param width width of the table
   * @return preferred height at that width
   */
  static float prefHeight(Table table, float width) {
    float height = table.getPrefHeight();
    // Wrapped labels learn their width in a layout pass; the second pass measures their height.
    for (int pass = 0; pass < 2; pass++) {
      table.setSize(width, height);
      table.validate();
      table.invalidate();
      height = table.getPrefHeight();
    }
    return height;
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

  // An ellipse in rows of two pixels.
  static void ellipse(
      Batch batch, Color color, float alpha, float centerX, float centerY, float rx, float ry) {
    for (float dy = -ry; dy < ry; dy += 2) {
      float row = (dy + 1) / ry;
      float half = rx * (float) Math.sqrt(Math.max(0, 1 - row * row));
      CanvasGraphics.fill(batch, color, alpha, centerX - half, centerY + dy, 2 * half, 2);
    }
  }

  // The sloped walls of a hopper around an outlet in the middle of its bottom edge.
  static void funnel(
      Batch batch, float alpha, float x, float y, float width, float height, float outlet) {
    for (float row = 0; row < height; row += 2) {
      float gap = outlet + (width - outlet) * row / height;
      float wall = (width - gap) / 2;
      CanvasGraphics.fill(batch, STEEL, alpha, x, y + row, wall, 2);
      CanvasGraphics.fill(batch, STEEL_DARK, alpha, x + width - wall, y + row, wall, 2);
      CanvasGraphics.fill(batch, RIVET_SHINE, alpha, x + wall - 2, y + row, 2, 2);
    }
  }

  /**
   * A steel bowl seen from slightly above: rim, hollow and the outer wall below the rim.
   *
   * @param batch batch to draw with
   * @param alpha inherited alpha
   * @param x left edge
   * @param y bottom edge, where the bowl stands
   * @param width bowl width
   * @param height bowl height
   * @param edge color around the rim, e.g. while a chip is held over it; {@code null} for none
   */
  static void bowl(
      Batch batch, float alpha, float x, float y, float width, float height, Color edge) {
    Vector2 floor = hollow(x, y, width, height);
    float rx = width / 2;
    float ry = rim(height);
    float centerY = floor.y + 5;
    ellipse(batch, Color.BLACK, alpha * .3f, floor.x + 8, y + 10, rx * .85f, ry * .55f);
    ellipse(batch, BOWL_BODY, alpha, floor.x, centerY - .6f * ry, rx * .9f, 1.2f * ry);
    if (edge != null) ellipse(batch, edge, alpha, floor.x, centerY, rx, ry);
    ellipse(batch, BOWL_RIM, alpha, floor.x, centerY, rx - (edge != null ? 4 : 0), ry - 3);
    ellipse(batch, BOWL_DEEP, alpha, floor.x, centerY, rx - 10, ry - 9);
    // The floor lies lower and nearer; the wall behind it stays in the rim's shadow.
    ellipse(batch, BOWL_FLOOR, alpha, floor.x, floor.y, rx - 18, ry - 14);
  }

  /**
   * Returns the middle of a bowl's floor, where a chip in the bowl lies.
   *
   * @param x left edge of the bowl
   * @param y bottom edge of the bowl
   * @param width bowl width
   * @param height bowl height
   * @return center of the floor
   */
  static Vector2 hollow(float x, float y, float width, float height) {
    return new Vector2(x + width / 2, y + 6 + 1.8f * rim(height) - 5);
  }

  // Vertical radius of the rim; the outer wall below it reaches down 1.8 times as far.
  private static float rim(float height) {
    return (height - 6) / 2.8f;
  }

  // The top of a wooden workbench with its front edge.
  static void bench(Batch batch, float alpha, float x, float y, float width, float height) {
    CanvasGraphics.fill(batch, WOOD, alpha, x, y, width, height);
    for (float grain = y + 22; grain < y + height - 4; grain += 18)
      CanvasGraphics.fill(batch, WOOD_GRAIN, alpha, x, grain, width, 2);
    CanvasGraphics.fill(batch, WOOD_EDGE, alpha, x, y, width, 14);
    CanvasGraphics.outline(batch, OUTLINE, alpha, x, y, width, height, 1);
  }
}
