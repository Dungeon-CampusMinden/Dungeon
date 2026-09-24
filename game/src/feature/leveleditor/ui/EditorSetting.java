package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogDesign;
import java.util.Objects;

/** Base class for settings shown in the level editor. */
public abstract class EditorSetting extends Table {

  private static final int LABEL_FONT_SIZE = 18;
  private static final int NESTED_LABEL_FONT_SIZE = 14;

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
    labelActor = Scene2dElementFactory.createLabel(
            this.label,
            this.isNested ? NESTED_LABEL_FONT_SIZE : LABEL_FONT_SIZE,
            ModeDetailsPanel.TEXT_COLOR);
    add(labelActor)
        .growX()
        .left();
  }

  /** Applies the standard smaller label style used by settings nested in a compound setting. */
  protected final void useNestedLabelStyle() {
    Label.LabelStyle style = new Label.LabelStyle(labelActor.getStyle());
    style.font =
        FontHelper.getFont(
            DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(NESTED_LABEL_FONT_SIZE));
    labelActor.setStyle(style);
  }
}
