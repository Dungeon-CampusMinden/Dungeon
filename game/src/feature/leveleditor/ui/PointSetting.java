package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.utils.Point;
import engine.utils.Scene2dElementFactory;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A labeled finite world-point setting with editable coordinates and a cursor assignment action.
 */
public class PointSetting extends Table {

  private static final int FONT_SIZE = 16;

  private final Supplier<Point> getter;
  private final Consumer<Point> setter;
  private final FiniteFloatSetting xSetting;
  private final FiniteFloatSetting ySetting;

  /**
   * Creates a point setting.
   *
   * <p>Activating the cursor button passes this setting's value consumer to {@code
   * cursorAssignmentRequester}. A mode can retain that consumer and invoke it with the next world
   * cursor click.
   *
   * @param label the setting label
   * @param getter supplies the current point
   * @param setter applies a new point
   * @param cursorAssignmentRequester starts assignment from the world cursor
   */
  public PointSetting(
      String label,
      Supplier<Point> getter,
      Consumer<Point> setter,
      Consumer<Consumer<Point>> cursorAssignmentRequester) {
    this.getter = Objects.requireNonNull(getter, "getter");
    this.setter = Objects.requireNonNull(setter, "setter");
    Objects.requireNonNull(cursorAssignmentRequester, "cursorAssignmentRequester");

    checked(getter.get());
    xSetting =
        new FiniteFloatSetting(
            "X",
            -Float.MAX_VALUE,
            Float.MAX_VALUE,
            () -> checked(getter.get()).x(),
            x -> value(new Point(x, checked(getter.get()).y())));
    ySetting =
        new FiniteFloatSetting(
            "Y",
            -Float.MAX_VALUE,
            Float.MAX_VALUE,
            () -> checked(getter.get()).y(),
            y -> value(new Point(checked(getter.get()).x(), y)));

    TextButton cursorButton =
        Scene2dElementFactory.createButton("Use World Cursor", "default", FONT_SIZE);
    cursorButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            cursorAssignmentRequester.accept(PointSetting.this::value);
          }
        });

    add(Scene2dElementFactory.createLabel(label, FONT_SIZE, ModeDetailsPanel.TEXT_COLOR))
        .growX()
        .left()
        .row();
    Table coordinates = new Table();
    coordinates.add(xSetting).growX().padRight(4f);
    coordinates.add(ySetting).growX();
    add(coordinates).growX().padTop(4f).row();
    add(cursorButton).growX().height(40f).padTop(4f);
  }

  /**
   * Returns the current point.
   *
   * @return current point
   */
  public Point value() {
    return checked(getter.get());
  }

  /**
   * Applies a finite point and refreshes both coordinate fields.
   *
   * @param point new point
   */
  public void value(Point point) {
    setter.accept(checked(point));
    refresh();
  }

  /** Synchronizes both displayed coordinates with the current point. */
  public void refresh() {
    xSetting.refresh();
    ySetting.refresh();
  }

  private static Point checked(Point point) {
    Objects.requireNonNull(point, "point");
    if (!Float.isFinite(point.x()) || !Float.isFinite(point.y())) {
      throw new IllegalArgumentException("point coordinates must be finite");
    }
    return point;
  }
}
