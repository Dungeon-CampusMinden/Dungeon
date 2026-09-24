package feature.leveleditor.ui;

import engine.utils.Point;
import feature.prefabs.Region;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A region setting with independently editable world coordinates for each normalized corner. */
public final class RegionSetting extends EditorSetting {

  private final Supplier<Region> getter;
  private final Consumer<Region> setter;
  private final PointSetting bottomLeftSetting;
  private final PointSetting topRightSetting;

  /**
   * Creates a region setting.
   *
   * @param label setting label
   * @param getter current normalized region
   * @param setter applies a region
   * @param cursorAssignmentRequester starts assignment from the world cursor
   */
  public RegionSetting(
      String label,
      Supplier<Region> getter,
      Consumer<Region> setter,
      Consumer<Consumer<Point>> cursorAssignmentRequester) {
    super(label);
    this.getter = Objects.requireNonNull(getter, "getter");
    this.setter = Objects.requireNonNull(setter, "setter");
    Objects.requireNonNull(cursorAssignmentRequester, "cursorAssignmentRequester");
    checked(getter.get());

    bottomLeftSetting =
        new PointSetting(
            "Bottom Left",
            () -> checked(this.getter.get()).bottomLeft(),
            point -> value(new Region(point, checked(this.getter.get()).topRight())),
            assignment -> cursorAssignmentRequester.accept(point -> {
              assignment.accept(point);
              refresh();
            }),
            true);
    topRightSetting =
        new PointSetting(
            "Top Right",
            () -> checked(this.getter.get()).topRight(),
            point -> value(new Region(checked(this.getter.get()).bottomLeft(), point)),
            assignment -> cursorAssignmentRequester.accept(point -> {
              assignment.accept(point);
              refresh();
            }),
            true);
    bottomLeftSetting.useNestedLabelStyle();
    topRightSetting.useNestedLabelStyle();
    row();
    add(bottomLeftSetting).growX().row();
    add(topRightSetting).growX().padTop(4f);
  }

  /**
   * Returns the current region.
   *
   * @return current region
   */
  public Region value() {
    return checked(getter.get());
  }

  /**
   * Applies a region and refreshes both corner controls.
   *
   * @param region new region
   */
  public void value(Region region) {
    setter.accept(checked(region));
    refresh();
  }

  /** Synchronizes both corner controls with the current region. */
  public void refresh() {
    bottomLeftSetting.refresh();
    topRightSetting.refresh();
  }

  private static Region checked(Region region) {
    return Objects.requireNonNull(region, "region");
  }
}
