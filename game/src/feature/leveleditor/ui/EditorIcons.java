package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Draws the small monochrome icons of the level editor panels, e.g. the search glass or the
 * disclosure arrows of the prefab list.
 *
 * <p>Icons are drawn white on transparent into a 64 pixel square and shown smaller with mipmaps, so
 * they stay smooth at any size and can be tinted.
 */
final class EditorIcons {

  private static final int SIZE = 64;

  private EditorIcons() {}

  /**
   * Draws an icon.
   *
   * @param name icon name
   * @return the icon, a white square with a cross for unknown names
   */
  static TextureRegionDrawable create(String name) {
    Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
    pixmap.setBlending(Pixmap.Blending.None);
    pixmap.setColor(0f, 0f, 0f, 0f);
    pixmap.fill();
    pixmap.setBlending(Pixmap.Blending.SourceOver);
    pixmap.setColor(Color.WHITE);
    Pen pen = new Pen(pixmap);
    switch (name) {
      case "chevron-right" -> pixmap.fillTriangle(20, 10, 20, 54, 48, 32);
      case "chevron-left" -> {
        pen.line(40, 14, 22, 32, 7);
        pen.line(22, 32, 40, 50, 7);
      }
      case "chevron-down" -> pixmap.fillTriangle(10, 20, 54, 20, 32, 48);
      case "search" -> {
        pen.ring(27, 27, 16, 5);
        pen.line(39, 39, 54, 54, 6);
      }
      case "plus" -> {
        pen.line(32, 12, 32, 52, 6);
        pen.line(12, 32, 52, 32, 6);
      }
      case "close" -> {
        pen.line(16, 16, 48, 48, 6);
        pen.line(48, 16, 16, 48, 6);
      }
      case "copy" -> {
        pen.frame(8, 18, 34, 38, 5);
        pen.frame(22, 6, 34, 38, 5);
      }
      case "trash" -> {
        pen.line(10, 16, 54, 16, 5);
        pen.line(24, 8, 40, 8, 5);
        pen.line(24, 8, 24, 16, 4);
        pen.line(40, 8, 40, 16, 4);
        pen.line(16, 22, 20, 56, 5);
        pen.line(48, 22, 44, 56, 5);
        pen.line(20, 56, 44, 56, 5);
        pen.line(28, 26, 28, 48, 4);
        pen.line(36, 26, 36, 48, 4);
      }
      case "pencil" -> {
        pen.line(14, 50, 46, 18, 9);
        pen.line(46, 18, 52, 12, 9);
        pixmap.fillTriangle(8, 56, 10, 44, 20, 54);
      }
      case "target" -> {
        pen.ring(32, 32, 17, 5);
        pen.line(32, 4, 32, 20, 5);
        pen.line(32, 44, 32, 60, 5);
        pen.line(4, 32, 20, 32, 5);
        pen.line(44, 32, 60, 32, 5);
        pen.dot(32, 32, 5);
      }
      case "frame" -> {
        pen.line(6, 6, 22, 6, 6);
        pen.line(6, 6, 6, 22, 6);
        pen.line(42, 6, 58, 6, 6);
        pen.line(58, 6, 58, 22, 6);
        pen.line(6, 58, 22, 58, 6);
        pen.line(6, 42, 6, 58, 6);
        pen.line(42, 58, 58, 58, 6);
        pen.line(58, 42, 58, 58, 6);
        pen.dot(32, 32, 8);
      }
      case "go" -> {
        pen.line(12, 32, 50, 32, 6);
        pen.line(34, 16, 50, 32, 6);
        pen.line(34, 48, 50, 32, 6);
      }
      case "play" -> pixmap.fillTriangle(18, 10, 18, 54, 54, 32);
      case "link" -> {
        pen.ring(22, 42, 13, 5);
        pen.ring(42, 22, 13, 5);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.fillCircle(32, 32, 7);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(Color.WHITE);
        pen.line(25, 39, 39, 25, 5);
      }
      case "folder" -> {
        pixmap.fillRectangle(6, 14, 22, 8);
        pixmap.fillRectangle(6, 20, 52, 32);
      }
      case "folder-open" -> {
        pixmap.fillRectangle(6, 14, 22, 8);
        pixmap.fillRectangle(6, 20, 8, 32);
        pixmap.fillTriangle(14, 52, 58, 52, 22, 28);
        pixmap.fillTriangle(22, 28, 58, 52, 62, 28);
      }
      case "stack" -> {
        pen.frame(6, 22, 36, 36, 5);
        pen.line(16, 14, 48, 14, 5);
        pen.line(48, 14, 48, 46, 5);
        pen.line(24, 6, 56, 6, 4);
        pen.line(56, 6, 56, 38, 4);
      }
      case "warning" -> {
        pixmap.fillTriangle(32, 4, 2, 58, 62, 58);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fillRectangle(29, 22, 7, 20);
        pixmap.fillRectangle(29, 46, 7, 7);
      }
      case "error" -> {
        pixmap.fillCircle(32, 32, 29);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fillRectangle(28, 14, 8, 23);
        pixmap.fillRectangle(28, 42, 8, 8);
      }
      case "power" -> {
        pen.arc(32, 35, 21, 5);
        pen.line(32, 6, 32, 30, 6);
      }
      case "eye" -> {
        pen.ellipse(32, 32, 27, 16, 5);
        pen.dot(32, 32, 9);
      }
      case "dots" -> {
        pen.dot(12, 32, 6);
        pen.dot(32, 32, 6);
        pen.dot(52, 32, 6);
      }
      case "sort-alpha" -> {
        pen.line(8, 50, 20, 14, 5);
        pen.line(20, 14, 32, 50, 5);
        pen.line(13, 38, 27, 38, 4);
        pen.line(44, 12, 44, 52, 5);
        pen.line(36, 42, 44, 52, 5);
        pen.line(52, 42, 44, 52, 5);
      }
      case "sort-order" -> {
        pen.line(8, 14, 32, 14, 5);
        pen.line(8, 32, 26, 32, 5);
        pen.line(8, 50, 20, 50, 5);
        pen.line(46, 10, 46, 52, 5);
        pen.line(38, 42, 46, 52, 5);
        pen.line(54, 42, 46, 52, 5);
      }
      case "grip" -> {
        for (int y = 16; y <= 48; y += 16) {
          pen.dot(24, y, 5);
          pen.dot(40, y, 5);
        }
      }
      case "checkbox-off", "checkbox-over" -> {
        pixmap.setColor(name.equals("checkbox-over") ? EditorTheme.ACCENT : EditorTheme.LINE);
        pixmap.fillRectangle(4, 4, 56, 56);
        pixmap.setColor(EditorTheme.FIELD);
        pixmap.fillRectangle(9, 9, 46, 46);
      }
      case "checkbox-on" -> {
        pixmap.setColor(EditorTheme.ACCENT);
        pixmap.fillRectangle(4, 4, 56, 56);
        pixmap.setColor(Color.WHITE);
        pen.line(17, 33, 28, 44, 7);
        pen.line(28, 44, 48, 20, 7);
      }
      case "knob", "knob-over" -> {
        pixmap.setColor(name.equals("knob-over") ? Color.WHITE : EditorTheme.TEXT);
        pixmap.fillCircle(32, 32, 28);
      }
      default -> {
        pen.frame(6, 6, 52, 52, 5);
        pen.line(6, 6, 58, 58, 4);
      }
    }
    Texture texture = new Texture(pixmap, true);
    texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
    pixmap.dispose();
    TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(texture));
    drawable.setMinWidth(16f);
    drawable.setMinHeight(16f);
    if (name.startsWith("checkbox")) {
      drawable.setMinWidth(18f);
      drawable.setMinHeight(18f);
    } else if (name.startsWith("knob")) {
      drawable.setMinWidth(14f);
      drawable.setMinHeight(14f);
    }
    return drawable;
  }

  /**
   * Draws thick strokes with round ends into a pixmap.
   *
   * @param pixmap pixmap to draw into
   */
  private record Pen(Pixmap pixmap) {

    void dot(int x, int y, int radius) {
      pixmap.fillCircle(x, y, radius);
    }

    void line(int x1, int y1, int x2, int y2, int width) {
      float length = (float) Math.hypot(x2 - x1, y2 - y1);
      int steps = Math.max(1, (int) Math.ceil(length));
      int radius = Math.max(1, width / 2);
      for (int step = 0; step <= steps; step++) {
        float t = step / (float) steps;
        pixmap.fillCircle(Math.round(x1 + (x2 - x1) * t), Math.round(y1 + (y2 - y1) * t), radius);
      }
    }

    void ring(int x, int y, int radius, int width) {
      ellipse(x, y, radius, radius, width);
    }

    void ellipse(int x, int y, int radiusX, int radiusY, int width) {
      int steps = 120;
      int dotRadius = Math.max(1, width / 2);
      for (int step = 0; step < steps; step++) {
        double angle = step * Math.PI * 2 / steps;
        pixmap.fillCircle(
            (int) Math.round(x + Math.cos(angle) * radiusX),
            (int) Math.round(y + Math.sin(angle) * radiusY),
            dotRadius);
      }
    }

    // An open circle with the gap at the top, like a power symbol.
    void arc(int x, int y, int radius, int width) {
      int steps = 120;
      int dotRadius = Math.max(1, width / 2);
      for (int step = 0; step < steps; step++) {
        double angle = step * Math.PI * 2 / steps;
        double fromTop = Math.abs(Math.atan2(Math.cos(angle), -Math.sin(angle)));
        if (fromTop < 0.6) continue;
        pixmap.fillCircle(
            (int) Math.round(x + Math.cos(angle) * radius),
            (int) Math.round(y + Math.sin(angle) * radius),
            dotRadius);
      }
    }

    void frame(int x, int y, int width, int height, int stroke) {
      line(x, y, x + width, y, stroke);
      line(x + width, y, x + width, y + height, stroke);
      line(x + width, y + height, x, y + height, stroke);
      line(x, y + height, x, y, stroke);
    }
  }
}
