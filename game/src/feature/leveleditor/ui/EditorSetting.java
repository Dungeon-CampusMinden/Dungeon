package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.Objects;

/** Base class for settings shown in the level editor. */
public abstract class EditorSetting extends Table {

  private static final int LABEL_FONT_SIZE = 14;
  private static final int NESTED_LABEL_FONT_SIZE = 13;

  private final String label;
  private final boolean isNested;
  private final Label labelActor;

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
    defaults().minWidth(0f);
    labelActor =
        EditorTheme.label(
            this.label,
            this.isNested ? NESTED_LABEL_FONT_SIZE : LABEL_FONT_SIZE,
            EditorTheme.TEXT_MUTED);
    labelActor.setWrap(true);
    add(labelActor).growX().left();
  }

  /** Applies the standard smaller label style used by settings nested in a compound setting. */
  protected final void useNestedLabelStyle() {
    Label.LabelStyle style = new Label.LabelStyle(labelActor.getStyle());
    style.font = EditorTheme.font(NESTED_LABEL_FONT_SIZE);
    labelActor.setStyle(style);
  }
}
