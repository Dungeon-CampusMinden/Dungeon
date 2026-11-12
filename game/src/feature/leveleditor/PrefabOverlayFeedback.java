package feature.leveleditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import engine.utils.Point;
import feature.leveleditor.ui.EditorTheme;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.Region;
import feature.systems.DebugDrawSystem;

/**
 * Draws the editor feedback of one prefab instance: its markers and regions in the world, its name
 * and warnings in the {@link EditorOverlay}.
 *
 * <p>Markers have the color of the prefab's category. The name is only drawn when the editor wants
 * it shown, e.g. for the selected or hovered instance, see {@link Look#showName()}. Warnings are
 * always drawn.
 */
final class PrefabOverlayFeedback implements PrefabEditorFeedback {

  private static final float MARKER_RADIUS = 0.085f;
  private static final float SELECTED_MARKER_RADIUS = 0.11f;
  private static final float HANDLE_RADIUS = 0.06f;
  private static final float LABEL_GAP = 4f;
  private static final int NAME_FONT_SIZE = 15;
  private static final int DETAIL_FONT_SIZE = 13;
  private static final Color DRAG_ORIGINAL = new Color(1f, 0.15f, 0.1f, 1f);
  private static final Color RING = new Color(1f, 1f, 1f, 0.95f);

  /**
   * How an instance is drawn.
   *
   * @param color marker color, usually the category color
   * @param selected whether the instance is selected
   * @param hovered whether the cursor hovers the instance, in the world or in the list
   * @param active whether the instance is active
   * @param dimmed whether the instance is unrelated to the current task, e.g. while picking a link
   * @param showName whether names and details are drawn
   * @param nameLook colors of the name label
   * @param namePriority overlay priority of the name label
   * @param nameSuffix text appended to the name, may be empty
   * @param highlightedPoint marker drawn as the original position of a drag, or {@code null}
   */
  record Look(
      Color color,
      boolean selected,
      boolean hovered,
      boolean active,
      boolean dimmed,
      boolean showName,
      EditorOverlay.Look nameLook,
      int namePriority,
      String nameSuffix,
      Point highlightedPoint) {}

  private final EditorOverlay overlay;
  private final Look look;

  /**
   * Creates the feedback of one instance.
   *
   * @param overlay overlay receiving the labels
   * @param look how the instance is drawn
   */
  PrefabOverlayFeedback(EditorOverlay overlay, Look look) {
    this.overlay = overlay;
    this.look = look;
  }

  @Override
  public void point(Point point, String label) {
    boolean dragOrigin = point.equals(look.highlightedPoint());
    float radius = look.selected() ? SELECTED_MARKER_RADIUS : MARKER_RADIUS;
    Color color = dragOrigin ? DRAG_ORIGINAL : markerColor();
    if (look.selected() || look.hovered()) {
      DebugDrawSystem.drawPoint(point, radius + 0.035f, look.selected() ? RING : ringColor());
    }
    if (look.active() || look.selected()) {
      DebugDrawSystem.drawPoint(point, radius, color);
    } else {
      DebugDrawSystem.drawCircleOutline(point, radius, color);
      DebugDrawSystem.drawCircleOutline(point, radius - 0.02f, color);
    }
    name(point, label, EditorOverlay.Anchor.ABOVE, screenRadius(point, radius) + LABEL_GAP);
  }

  @Override
  public void line(Point from, Point to, boolean arrow) {
    DebugDrawSystem.drawLine(from, to, arrow, geometryColor());
  }

  @Override
  public void line(Point from, Point to, boolean arrow, Color color) {
    if (color == null) {
      line(from, to, arrow);
      return;
    }
    Color result = color.cpy();
    result.a *= geometryColor().a;
    DebugDrawSystem.drawLine(from, to, arrow, result);
  }

  @Override
  public void rectangle(Point firstCorner, Point secondCorner) {
    float x = Math.min(firstCorner.x(), secondCorner.x());
    float y = Math.min(firstCorner.y(), secondCorner.y());
    float width = Math.abs(secondCorner.x() - firstCorner.x());
    float height = Math.abs(secondCorner.y() - firstCorner.y());
    DebugDrawSystem.drawRectangleOutline(x, y, width, height, geometryColor());
  }

  @Override
  public void region(Region region, String label) {
    Point bottomLeft = region.bottomLeft();
    Point topRight = region.topRight();
    if (bottomLeft.equals(topRight)) {
      point(bottomLeft, label);
      return;
    }
    rectangle(bottomLeft, topRight);
    // The corners are drag handles, shown in full while the region is in focus.
    boolean focused = look.selected() || look.hovered();
    Color handle = focused ? markerColor() : EditorTheme.alpha(markerColor(), 0.45f);
    float radius = focused ? HANDLE_RADIUS : HANDLE_RADIUS * 0.6f;
    for (Point corner : new Point[] {bottomLeft, topRight}) {
      DebugDrawSystem.drawPoint(
          corner, radius, corner.equals(look.highlightedPoint()) ? DRAG_ORIGINAL : handle);
    }
    Point center =
        new Point((bottomLeft.x() + topRight.x()) * 0.5f, (bottomLeft.y() + topRight.y()) * 0.5f);
    name(center, label, EditorOverlay.Anchor.CENTER, 0f);
  }

  @Override
  public void label(Point position, String text) {
    if (!look.showName()) return;
    overlay.text(
        text,
        EditorOverlay.toScreen(position),
        EditorOverlay.Anchor.CENTER,
        0f,
        DETAIL_FONT_SIZE,
        look.selected() ? EditorTheme.LABEL_FOCUSED : look.nameLook(),
        look.namePriority() - 1,
        false);
  }

  @Override
  public void warning(Point position, String text) {
    overlay.text(
        text,
        EditorOverlay.toScreen(position),
        EditorOverlay.Anchor.BELOW,
        screenRadius(position, MARKER_RADIUS) + LABEL_GAP,
        DETAIL_FONT_SIZE,
        EditorTheme.LABEL_WARNING,
        EditorOverlay.PRIORITY_WARNING,
        false);
  }

  private void name(Point position, String label, EditorOverlay.Anchor anchor, float offset) {
    if (!look.showName() || label == null || label.isBlank()) return;
    overlay.text(
        label + look.nameSuffix(),
        EditorOverlay.toScreen(position),
        anchor,
        offset,
        NAME_FONT_SIZE,
        look.nameLook(),
        look.namePriority(),
        look.selected());
  }

  private Color markerColor() {
    if (look.selected()) return EditorTheme.SELECTION;
    if (!look.active()) return EditorTheme.alpha(EditorTheme.TEXT_MUTED, 0.8f);
    return EditorTheme.alpha(look.color(), look.dimmed() ? 0.3f : 0.9f);
  }

  private Color ringColor() {
    return look.dimmed() ? EditorTheme.alpha(RING, 0.4f) : RING;
  }

  private Color geometryColor() {
    if (look.selected()) return EditorTheme.SELECTION;
    if (look.hovered()) return EditorTheme.alpha(look.color(), 0.9f);
    float alpha = look.dimmed() ? 0.15f : 0.4f;
    if (!look.active()) return EditorTheme.alpha(EditorTheme.TEXT_MUTED, alpha);
    return EditorTheme.alpha(look.color(), alpha);
  }

  private static float screenRadius(Point position, float radius) {
    Vector2 center = EditorOverlay.toScreen(position);
    Vector2 edge = EditorOverlay.toScreen(position.translate(radius, 0f));
    return Math.abs(edge.x - center.x);
  }
}
