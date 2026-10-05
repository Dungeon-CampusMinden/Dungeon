package feature.leveleditor.ui;

import engine.utils.Vector2;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A finite two-component vector setting with no world-cursor interaction. */
public final class Vector2Setting extends EditorSetting {

  private final Supplier<Vector2> getter;
  private final Consumer<Vector2> setter;
  private final FloatSetting xSetting;
  private final FloatSetting ySetting;

  /**
   * Creates a vector setting.
   *
   * @param label setting label
   * @param getter current vector
   * @param setter applies a vector
   */
  public Vector2Setting(String label, Supplier<Vector2> getter, Consumer<Vector2> setter) {
    super(label);
    this.getter = Objects.requireNonNull(getter, "getter");
    this.setter = Objects.requireNonNull(setter, "setter");
    checked(getter.get());
    xSetting =
        new FloatSetting(
            "X",
            -Float.MAX_VALUE,
            Float.MAX_VALUE,
            () -> checked(this.getter.get()).x(),
            x -> value(Vector2.of(x, checked(this.getter.get()).y())),
            true,
            true);
    ySetting =
        new FloatSetting(
            "Y",
            -Float.MAX_VALUE,
            Float.MAX_VALUE,
            () -> checked(this.getter.get()).y(),
            y -> value(Vector2.of(checked(this.getter.get()).x(), y)),
            true,
            true);
    row();
    add(xSetting).growX().padRight(8f);
    add(ySetting).growX();
  }

  /**
   * Returns the current vector.
   *
   * @return current vector
   */
  public Vector2 value() {
    return checked(getter.get());
  }

  /**
   * Applies a finite vector and refreshes both component controls.
   *
   * @param vector new finite vector
   */
  public void value(Vector2 vector) {
    setter.accept(checked(vector));
    refresh();
  }

  /** Synchronizes both component controls with the current vector. */
  public void refresh() {
    xSetting.refresh();
    ySetting.refresh();
  }

  private static Vector2 checked(Vector2 vector) {
    Objects.requireNonNull(vector, "vector");
    if (!Float.isFinite(vector.x()) || !Float.isFinite(vector.y())) {
      throw new IllegalArgumentException("vector components must be finite");
    }
    return vector;
  }
}
