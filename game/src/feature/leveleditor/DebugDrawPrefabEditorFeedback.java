package feature.leveleditor;

import com.badlogic.gdx.graphics.Color;
import engine.utils.Point;
import feature.prefabs.PrefabEditorFeedback;
import feature.systems.DebugDrawSystem;

/** Renders prefab editor feedback with a consistent selected or unselected visual style. */
public final class DebugDrawPrefabEditorFeedback implements PrefabEditorFeedback {

  private static final Style SELECTED_STYLE =
      new Style(new Color(1f, 0.75f, 0.1f, 1f), Color.WHITE, 0.09f);
  private static final Style UNSELECTED_STYLE =
      new Style(new Color(0.25f, 0.85f, 1f, 0.35f), new Color(1f, 1f, 1f, 0.45f), 0.09f);

  private final Style style;

  /**
   * Creates feedback using the standard selection style.
   *
   * @param selected whether the rendered prefab is selected
   */
  public DebugDrawPrefabEditorFeedback(boolean selected) {
    this(selected ? SELECTED_STYLE : UNSELECTED_STYLE);
  }

  /**
   * Creates feedback with a custom reusable style.
   *
   * @param style drawing style
   */
  public DebugDrawPrefabEditorFeedback(Style style) {
    this.style = style;
  }

  @Override
  public void point(Point point, String label) {
    DebugDrawSystem.drawPoint(point, style.pointRadius(), style.geometryColor());
    if (label != null && !label.isBlank()) {
      DebugDrawSystem.drawTextInWorldCoordsCentered(
          label, point.translate(0f, style.pointRadius() + 0.2f), style.labelColor());
    }
  }

  @Override
  public void line(Point from, Point to, boolean arrow) {
    DebugDrawSystem.drawLine(from, to, arrow, style.geometryColor());
  }

  @Override
  public void rectangle(Point firstCorner, Point secondCorner) {
    float x = Math.min(firstCorner.x(), secondCorner.x());
    float y = Math.min(firstCorner.y(), secondCorner.y());
    float width = Math.abs(secondCorner.x() - firstCorner.x());
    float height = Math.abs(secondCorner.y() - firstCorner.y());
    DebugDrawSystem.drawRectangleOutline(x, y, width, height, style.geometryColor());
  }

  @Override
  public void label(Point position, String text) {
    if (text != null && !text.isBlank()) {
      DebugDrawSystem.drawTextInWorldCoordsCentered(text, position, style.labelColor());
    }
  }

  /**
   * Visual configuration for prefab feedback.
   *
   * @param geometryColor point, line, arrow, and rectangle color
   * @param labelColor label color
   * @param pointRadius point marker radius in world units
   */
  public record Style(Color geometryColor, Color labelColor, float pointRadius) {

    /**
     * Validates a prefab feedback style.
     *
     * @param geometryColor point, line, arrow, and rectangle color
     * @param labelColor label color
     * @param pointRadius point marker radius in world units
     */
    public Style {
      if (geometryColor == null || labelColor == null) {
        throw new IllegalArgumentException("feedback colors must not be null");
      }
      if (!Float.isFinite(pointRadius) || pointRadius <= 0f) {
        throw new IllegalArgumentException("point radius must be positive and finite");
      }
    }
  }
}
