package feature.leveleditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import engine.Game;
import engine.systems.CameraSystem;
import engine.utils.FontHelper;
import engine.utils.Point;
import engine.utils.components.draw.BlendUtils;
import engine.utils.components.draw.ColorUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Screen-space overlay of the level editor: text on dark plates, markers and badges drawn on top of
 * the world with a constant size in pixels.
 *
 * <p>Requests are queued while a mode renders and drawn together by {@link #flush()}. Pinned labels
 * and dots are placed first, exactly where they were requested, also at the edge of the screen,
 * e.g. the rows of a list that can be clicked where they are drawn. The other labels follow,
 * highest priority first, moved onto the screen: a label that would cover a pinned label or a dot
 * moves up or down until it is clear of them. A label that would cover an already placed one tries
 * its alternative side and is left out if that is taken too, unless it is forced. Dense areas stay
 * readable that way, the most important labels win.
 */
public final class EditorOverlay {

  /** Where a label sits relative to its position. */
  public enum Anchor {
    /** Centered above the position. */
    ABOVE,
    /** Centered below the position. */
    BELOW,
    /** Centered on the position. */
    CENTER,
    /** Starting right of the position, vertically centered. */
    RIGHT,
    /** Ending left of the position, vertically centered. */
    LEFT
  }

  /**
   * Colors of a label.
   *
   * @param text text color
   * @param plate background color, or {@code null} for no plate
   */
  public record Look(Color text, Color plate) {}

  /** Priority of the selected instance's labels. */
  public static final int PRIORITY_SELECTED = 100;

  /** Priority of warnings, e.g. a door control without a door. */
  public static final int PRIORITY_WARNING = 95;

  /** Priority of the hovered instance's labels. */
  public static final int PRIORITY_HOVERED = 90;

  /** Priority of instances that can be clicked to finish a link. */
  public static final int PRIORITY_TARGET = 80;

  /** Priority of instances linked to the selection. */
  public static final int PRIORITY_RELATED = 60;

  /** Priority of link labels. */
  public static final int PRIORITY_LINK = 50;

  /** Priority of all other labels. */
  public static final int PRIORITY_NORMAL = 10;

  private static final String FONT_PATH = "fonts/Roboto-SemiBold.ttf";
  private static final float PAD_X = 5f;
  private static final float PAD_Y = 3f;
  private static final float CORNER = 4f;
  private static final float GAP = 2f;
  // Distance kept to the pinned labels and dots a label moves out of, a pixel more than GAP.
  private static final float CLEARANCE = GAP + 1f;
  private static final float DESCENT = 0.22f;
  private static final float EDGE = 4f;
  private static final float DOT_STEP = 10f;
  private static final float DOT_GAP = 3f;
  private static final float DOT_RADIUS = 3.6f;
  private static final float RING_WIDTH = 2f;

  private static ShapeRenderer shapes;
  private static SpriteBatch batch;
  private static final OrthographicCamera SCREEN = new OrthographicCamera();

  private final List<TextRequest> texts = new ArrayList<>();
  private final List<DotRequest> dots = new ArrayList<>();
  private final List<Rectangle> placed = new ArrayList<>();
  // Pinned labels and dots, which the other labels move out of.
  private final List<Rectangle> obstacles = new ArrayList<>();
  private int sequence;

  /**
   * Converts a world position into screen pixels, origin bottom left.
   *
   * @param world world position
   * @return screen position
   */
  public static Vector2 toScreen(Point world) {
    Vector3 projected = CameraSystem.camera().project(new Vector3(world.x(), world.y(), 0f));
    return new Vector2(projected.x, projected.y);
  }

  /**
   * Converts a screen position, origin bottom left, into a world position.
   *
   * @param screen screen position
   * @return world position
   */
  public static Point toWorld(Vector2 screen) {
    Vector3 world =
        CameraSystem.camera().unproject(new Vector3(screen.x, Game.windowHeight() - screen.y, 0f));
    return new Point(world.x, world.y);
  }

  /**
   * Returns the screen size of a text in the overlay font.
   *
   * @param text text
   * @param fontSize font size in pixels
   * @return width and height of the text, without its plate
   */
  public static Vector2 textSize(String text, int fontSize) {
    BitmapFont font = font(fontSize);
    if (text.isEmpty()) return new Vector2(0f, font.getCapHeight());
    GlyphLayout layout = new GlyphLayout(font, text);
    return new Vector2(layout.width, layout.height);
  }

  /**
   * Returns the screen size of a badge, see {@link #badge}.
   *
   * @param dots number of dots
   * @param text text after the dots, may be empty
   * @param fontSize font size of the text in pixels
   * @return width and height of the badge with its plate
   */
  public static Vector2 badgeSize(int dots, String text, int fontSize) {
    return plateSize(textSize(text, fontSize), dotsWidth(dots, text), fontSize);
  }

  /**
   * Queues a label.
   *
   * @param text label text
   * @param screen screen position the label is anchored at
   * @param anchor where the label sits relative to the position
   * @param offset distance from the position in pixels, not used by {@link Anchor#CENTER}
   * @param fontSize font size in pixels
   * @param look colors
   * @param priority higher priorities are placed first
   * @param force whether the label is drawn even if it covers another label
   */
  public void text(
      String text,
      Vector2 screen,
      Anchor anchor,
      float offset,
      int fontSize,
      Look look,
      int priority,
      boolean force) {
    if (text == null || text.isBlank()) return;
    texts.add(
        new TextRequest(
            text,
            List.of(),
            screen,
            anchor,
            offset,
            fontSize,
            look,
            priority,
            force,
            false,
            sequence++));
  }

  /**
   * Queues a pinned label: it is drawn exactly at its position, even if it covers another pinned
   * label or reaches past the edge of the screen, and the other labels move out of its way.
   *
   * @param text label text
   * @param screen screen position the label is anchored at
   * @param anchor where the label sits relative to the position
   * @param offset distance from the position in pixels, not used by {@link Anchor#CENTER}
   * @param fontSize font size in pixels
   * @param look colors
   */
  public void pinnedText(
      String text, Vector2 screen, Anchor anchor, float offset, int fontSize, Look look) {
    if (text == null || text.isBlank()) return;
    texts.add(
        new TextRequest(
            text, List.of(), screen, anchor, offset, fontSize, look, 0, true, true, sequence++));
  }

  /**
   * Queues a badge: a plate with colored dots, followed by an optional text. Badges are pinned like
   * {@link #pinnedText} labels.
   *
   * @param dots colors of the dots, in order
   * @param text text after the dots, may be empty
   * @param screen screen position the badge is anchored at
   * @param anchor where the badge sits relative to the position
   * @param offset distance from the position in pixels, not used by {@link Anchor#CENTER}
   * @param fontSize font size of the text in pixels, also decides the plate height
   * @param look colors of the text and plate
   */
  public void badge(
      List<Color> dots,
      String text,
      Vector2 screen,
      Anchor anchor,
      float offset,
      int fontSize,
      Look look) {
    texts.add(
        new TextRequest(
            text == null ? "" : text,
            List.copyOf(dots),
            screen,
            anchor,
            offset,
            fontSize,
            look,
            0,
            true,
            true,
            sequence++));
  }

  /**
   * Queues a filled dot, drawn below all labels. Dots are pinned: labels move out of their way.
   *
   * @param screen screen position of the center
   * @param radius radius in pixels
   * @param fill fill color
   * @param ring ring color around the dot, or {@code null}
   */
  public void dot(Vector2 screen, float radius, Color fill, Color ring) {
    dots.add(new DotRequest(screen, radius, fill, ring));
  }

  /** Places and draws all queued requests and clears the queue. */
  public void flush() {
    placed.clear();
    obstacles.clear();
    for (DotRequest dot : dots) obstacles.add(dot.bounds());
    List<Placement> placements = new ArrayList<>();
    texts.sort(
        Comparator.comparing((TextRequest request) -> !request.pinned())
            .thenComparing(Comparator.comparingInt(TextRequest::priority).reversed())
            .thenComparingInt(TextRequest::sequence));
    float width = Game.windowWidth();
    float height = Game.windowHeight();
    for (TextRequest request : texts) {
      Vector2 size = textSize(request.text(), request.fontSize());
      Rectangle box = box(request, request.anchor(), size);
      if (box.x > width || box.y > height || box.x + box.width < 0 || box.y + box.height < 0) {
        continue;
      }
      if (request.pinned()) {
        obstacles.add(box);
      } else {
        // Labels at the edge of the screen are moved in, so they can be read in full.
        clamp(box, width, height);
        box = fit(request, size, box, width, height);
        if (box == null) continue;
      }
      placed.add(box);
      placements.add(new Placement(request, box));
    }
    draw(placements);
    texts.clear();
    dots.clear();
    sequence = 0;
  }

  /**
   * Finds the place of a label that is not pinned: its box, or its box on the alternative side,
   * each moved out of the pinned labels and dots it covers.
   *
   * @param request the label
   * @param size text size of the label
   * @param preferred box at the requested position, moved onto the screen
   * @param width screen width
   * @param height screen height
   * @return the first place that covers no other label; for a forced label the first place clear of
   *     pinned labels and dots, or its preferred box; {@code null} if the label is left out
   */
  private Rectangle fit(
      TextRequest request, Vector2 size, Rectangle preferred, float width, float height) {
    List<Rectangle> candidates =
        new ArrayList<>(unblocked(preferred, request.anchor() == Anchor.BELOW, height));
    Anchor alternative = alternative(request.anchor());
    if (alternative != null) {
      Rectangle other = box(request, alternative, size);
      clamp(other, width, height);
      candidates.addAll(unblocked(other, alternative == Anchor.BELOW, height));
    }
    for (Rectangle candidate : candidates) {
      if (!overlaps(candidate, placed)) return candidate;
    }
    if (!request.force()) return null;
    return candidates.isEmpty() ? preferred : candidates.getFirst();
  }

  /**
   * Returns a box if it covers no pinned label or dot, otherwise the box moved just past them,
   * first in the given direction, then in the other one.
   *
   * @param box label box
   * @param downFirst whether moving down is tried first, e.g. for labels below their position
   * @param height screen height
   * @return the box, or the moved boxes that stay on the screen; may be empty
   */
  private List<Rectangle> unblocked(Rectangle box, boolean downFirst, float height) {
    if (!overlaps(box, obstacles)) return List.of(box);
    List<Rectangle> result = new ArrayList<>(2);
    for (boolean down : new boolean[] {downFirst, !downFirst}) {
      Rectangle moved = slide(box, down, height);
      if (moved != null) result.add(moved);
    }
    return result;
  }

  /* Moves a box up or down past the pinned labels and dots; null if it then leaves the screen. */
  private Rectangle slide(Rectangle box, boolean down, float height) {
    Rectangle moved = new Rectangle(box);
    for (int step = 0; step <= obstacles.size(); step++) {
      Rectangle obstacle = firstOverlap(moved, obstacles);
      if (obstacle == null) {
        return moved.y >= EDGE && moved.y + moved.height <= height - EDGE ? moved : null;
      }
      moved.y =
          down ? obstacle.y - CLEARANCE - moved.height : obstacle.y + obstacle.height + CLEARANCE;
    }
    return null;
  }

  private static void clamp(Rectangle box, float width, float height) {
    box.x = Math.max(EDGE, Math.min(box.x, width - EDGE - box.width));
    box.y = Math.max(EDGE, Math.min(box.y, height - EDGE - box.height));
  }

  private static boolean overlaps(Rectangle box, List<Rectangle> others) {
    return firstOverlap(box, others) != null;
  }

  /* Returns the first rectangle closer to the box than GAP, or null. */
  private static Rectangle firstOverlap(Rectangle box, List<Rectangle> others) {
    for (Rectangle other : others) {
      if (box.x < other.x + other.width + GAP
          && box.x + box.width + GAP > other.x
          && box.y < other.y + other.height + GAP
          && box.y + box.height + GAP > other.y) {
        return other;
      }
    }
    return null;
  }

  private static Anchor alternative(Anchor anchor) {
    return switch (anchor) {
      case ABOVE -> Anchor.BELOW;
      case BELOW -> Anchor.ABOVE;
      default -> null;
    };
  }

  private static Rectangle box(TextRequest request, Anchor anchor, Vector2 size) {
    Vector2 plate = plateSize(size, request.dotsWidth(), request.fontSize());
    float boxWidth = plate.x;
    float boxHeight = plate.y;
    Vector2 at = request.screen();
    return switch (anchor) {
      case ABOVE ->
          new Rectangle(at.x - boxWidth / 2f, at.y + request.offset(), boxWidth, boxHeight);
      case BELOW ->
          new Rectangle(
              at.x - boxWidth / 2f, at.y - request.offset() - boxHeight, boxWidth, boxHeight);
      case CENTER ->
          new Rectangle(at.x - boxWidth / 2f, at.y - boxHeight / 2f, boxWidth, boxHeight);
      case RIGHT ->
          new Rectangle(at.x + request.offset(), at.y - boxHeight / 2f, boxWidth, boxHeight);
      case LEFT ->
          new Rectangle(
              at.x - request.offset() - boxWidth, at.y - boxHeight / 2f, boxWidth, boxHeight);
    };
  }

  private static Vector2 plateSize(Vector2 textSize, float dotsWidth, int fontSize) {
    // Room for descenders below the baseline.
    return new Vector2(
        textSize.x + 2 * PAD_X + dotsWidth, textSize.y + 2 * PAD_Y + fontSize * DESCENT);
  }

  private static float dotsWidth(int dots, String text) {
    return dots == 0 ? 0f : dots * DOT_STEP + (text.isEmpty() ? 0f : DOT_GAP);
  }

  private void draw(List<Placement> placements) {
    if (placements.isEmpty() && dots.isEmpty()) return;
    if (shapes == null) {
      shapes = new ShapeRenderer();
      batch = new SpriteBatch();
    }
    SCREEN.setToOrtho(false, Game.windowWidth(), Game.windowHeight());
    BlendUtils.setBlending();
    shapes.setProjectionMatrix(SCREEN.combined);
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    for (DotRequest dot : dots) {
      if (dot.ring() != null) {
        shapes.setColor(ColorUtils.pmaColor(dot.ring()));
        shapes.circle(dot.screen().x, dot.screen().y, dot.radius() + RING_WIDTH, 20);
      }
      shapes.setColor(ColorUtils.pmaColor(dot.fill()));
      shapes.circle(dot.screen().x, dot.screen().y, dot.radius(), 20);
    }
    for (Placement placement : placements) {
      Color plate = placement.request().look().plate();
      if (plate != null) roundedRect(placement.box(), plate);
    }
    for (Placement placement : placements) {
      Rectangle box = placement.box();
      float x = box.x + PAD_X + DOT_STEP / 2f;
      for (Color dot : placement.request().dots()) {
        shapes.setColor(ColorUtils.pmaColor(dot));
        shapes.circle(x, box.y + box.height / 2f, DOT_RADIUS, 12);
        x += DOT_STEP;
      }
    }
    shapes.end();

    batch.setProjectionMatrix(SCREEN.combined);
    batch.begin();
    for (Placement placement : placements) {
      TextRequest request = placement.request();
      BitmapFont font = font(request.fontSize());
      font.setColor(request.look().text());
      Rectangle box = placement.box();
      if (request.text().isEmpty()) continue;
      font.draw(
          batch, request.text(), box.x + PAD_X + request.dotsWidth(), box.y + box.height - PAD_Y);
    }
    batch.end();
  }

  private static void roundedRect(Rectangle box, Color color) {
    shapes.setColor(ColorUtils.pmaColor(color));
    float radius = Math.min(CORNER, Math.min(box.width, box.height) / 2f);
    float x = Math.round(box.x);
    float y = Math.round(box.y);
    float width = Math.round(box.width);
    float height = Math.round(box.height);
    shapes.rect(x + radius, y, width - 2 * radius, height);
    shapes.rect(x, y + radius, radius, height - 2 * radius);
    shapes.rect(x + width - radius, y + radius, radius, height - 2 * radius);
    shapes.arc(x + radius, y + radius, radius, 180f, 90f, 6);
    shapes.arc(x + width - radius, y + radius, radius, 270f, 90f, 6);
    shapes.arc(x + width - radius, y + height - radius, radius, 0f, 90f, 6);
    shapes.arc(x + radius, y + height - radius, radius, 90f, 90f, 6);
  }

  private static BitmapFont font(int size) {
    return FontHelper.getFont(FONT_PATH, size, Color.WHITE, 0f, Color.BLACK);
  }

  private record TextRequest(
      String text,
      List<Color> dots,
      Vector2 screen,
      Anchor anchor,
      float offset,
      int fontSize,
      Look look,
      int priority,
      boolean force,
      boolean pinned,
      int sequence) {

    private float dotsWidth() {
      return EditorOverlay.dotsWidth(dots.size(), text);
    }
  }

  private record DotRequest(Vector2 screen, float radius, Color fill, Color ring) {

    private Rectangle bounds() {
      float outer = ring == null ? radius : radius + RING_WIDTH;
      return new Rectangle(screen.x - outer, screen.y - outer, outer * 2f, outer * 2f);
    }
  }

  private record Placement(TextRequest request, Rectangle box) {}
}
