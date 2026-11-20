package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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

  /**
   * Shows a description of the setting in the tooltip of an info icon next to its name: right
   * behind the name if the control is on the same line, else at the end of the line.
   *
   * @param description the description
   * @return this setting
   */
  public EditorSetting describe(String description) {
    Cell<Label> cell = getCell(labelActor);
    if (cell == null) return this;
    boolean inline = false;
    for (Cell<?> other : getCells()) {
      if (other != cell && other.getRow() == cell.getRow()) inline = true;
    }
    Table header = new Table();
    cell.setActor(header);
    if (inline) {
      // The name takes the width of its text, so the icon follows it. A long name still wraps.
      float width = new GlyphLayout(labelActor.getStyle().font, label).width;
      header.add(labelActor).left().minWidth(0f).prefWidth(width + 1f);
      header.add(EditorTheme.infoButton(description)).padLeft(5f);
      header.add().growX();
    } else {
      header.add(labelActor).left().growX().minWidth(0f);
      header.add(EditorTheme.infoButton(description)).padLeft(6f);
      // Spans the controls below, so the icon is at the end of the line.
      cell.colspan(Math.max(1, getColumns()));
    }
    return this;
  }

  /** Applies the standard smaller label style used by settings nested in a compound setting. */
  protected final void useNestedLabelStyle() {
    Label.LabelStyle style = new Label.LabelStyle(labelActor.getStyle());
    style.font = EditorTheme.font(NESTED_LABEL_FONT_SIZE);
    labelActor.setStyle(style);
  }
}
