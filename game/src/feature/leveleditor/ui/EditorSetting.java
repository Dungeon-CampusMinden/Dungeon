package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.utils.Scene2dElementFactory;
import java.util.Objects;

/** Base class for settings shown in the level editor. */
public abstract class EditorSetting extends Table {

  private static final int LABEL_FONT_SIZE = 18;
  private static final int NESTED_LABEL_FONT_SIZE = 14;

  private final String label;
  private final boolean isNested;

  /**
   * Creates a non-nested editor setting.
   *
   * @param label the text shown for the setting.
   */
  protected EditorSetting(String label) {
    this(label, false);
  }

  /**
   * Creates an editor setting.
   *
   * @param label the text shown for the setting.
   * @param isNested whether the setting is nested inside another setting.
   */
  protected EditorSetting(String label, boolean isNested) {
    this.label = Objects.requireNonNull(label, "label");
    this.isNested = isNested;
    add(Scene2dElementFactory.createLabel(
            this.label,
            this.isNested ? NESTED_LABEL_FONT_SIZE : LABEL_FONT_SIZE,
            ModeDetailsPanel.TEXT_COLOR))
        .growX()
        .left();
  }
}
