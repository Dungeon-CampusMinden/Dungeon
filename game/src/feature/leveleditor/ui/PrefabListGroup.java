package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Collapsible group of prefab list entries sharing a name part, such as an area prefix or a base
 * name.
 *
 * <p>The header shows a folder icon, the shared name and the number of prefabs. Members, which can
 * be nested groups, are indented below the header while the group is expanded. The rename button
 * swaps the header for a text field that renames the shared name part of every member.
 */
public final class PrefabListGroup extends Table {

  private static final int FONT_SIZE = 14;
  private static final float ROW_HEIGHT = 30f;
  private static final float ICON_SIZE = 16f;
  private static final float INDENT = 20f;
  private static final float GAP = 2f;
  private static final float ICON_BUTTON_PAD = 6f;

  private final String label;
  private final boolean expanded;
  private final Runnable onToggle;
  private final Consumer<String> onRename;
  private final Table headerRow = new Table();
  private final TextButton header;
  private final Table members = new Table();
  private final List<Actor> memberRows = new ArrayList<>();
  private boolean renaming;

  /**
   * Creates a prefab group.
   *
   * @param label shared name part of all members
   * @param memberCount number of prefabs in the group
   * @param expanded whether members are shown
   * @param highlighted whether the header uses the selection style
   * @param onToggle invoked when the header is clicked
   * @param onRename invoked with a changed label
   */
  public PrefabListGroup(
      String label,
      int memberCount,
      boolean expanded,
      boolean highlighted,
      Runnable onToggle,
      Consumer<String> onRename) {
    this.label = label;
    this.expanded = expanded;
    this.onToggle = onToggle;
    this.onRename = onRename;

    header =
        Scene2dElementFactory.createButton(
            label + " (" + memberCount + ")", highlighted ? "blue-outline" : "default", FONT_SIZE);
    header.getLabel().setAlignment(Align.left);
    header.getLabel().setEllipsis(true);
    header.clearChildren();
    Image icon =
        new Image(
            new TextureRegionDrawable(
                TextureMap.instance()
                    .textureAt(
                        new SimpleIPath(expanded ? "hud/folder_open.png" : "hud/folder.png"))));
    icon.setScaling(Scaling.fit);
    header.add(icon).size(ICON_SIZE).padRight(6f);
    header.add(header.getLabel()).growX().minWidth(0f);
    header.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            PrefabListGroup.this.onToggle.run();
          }
        });

    top().defaults().growX();
    members.top().defaults().growX();
    buildHeaderRow();
    add(headerRow).growX().height(ROW_HEIGHT).row();
    if (expanded) add(members).growX().padLeft(INDENT).row();
  }

  /**
   * Returns the shared name part of all members.
   *
   * @return group label
   */
  public String label() {
    return label;
  }

  /**
   * Returns whether members are currently shown.
   *
   * @return true when expanded
   */
  public boolean expanded() {
    return expanded;
  }

  /**
   * Returns the clickable header that toggles the group.
   *
   * @return header button
   */
  public TextButton header() {
    return header;
  }

  /**
   * Adds an indented member row.
   *
   * @param entry member entry actor or nested group
   */
  public void addMember(Actor entry) {
    Cell<Actor> cell = members.add(entry).growX().padTop(GAP);
    if (!(entry instanceof PrefabListGroup)) cell.height(ROW_HEIGHT);
    members.row();
    memberRows.add(entry);
  }

  /**
   * Returns the member rows in display order.
   *
   * @return member row actors
   */
  public List<Actor> memberRows() {
    return memberRows;
  }

  /** Replaces the header with a text field for renaming the shared name part. */
  public void startRename() {
    if (renaming) return;
    renaming = true;
    headerRow.clearChildren();
    TextField field = Scene2dElementFactory.createTextField(label, FONT_SIZE);
    field.setMessageText("Group name");
    field.setTextFieldListener(
        (textField, character) -> {
          if (character == '\r' || character == '\n') finishRename(textField.getText());
        });
    field.addListener(
        new FocusListener() {
          @Override
          public void keyboardFocusChanged(
              FocusListener.FocusEvent event, Actor actor, boolean focused) {
            if (!focused) finishRename(field.getText());
          }
        });
    field.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != Input.Keys.ESCAPE) return false;
            finishRename(null);
            return true;
          }
        });
    ImageButton confirm = Scene2dElementFactory.createIconButton("hud/check.png", "blue-outline");
    confirm.pad(ICON_BUTTON_PAD);
    confirm.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            finishRename(field.getText());
          }
        });
    headerRow.add(field).growX().minWidth(0f).height(ROW_HEIGHT);
    headerRow.add(confirm).size(ROW_HEIGHT).padLeft(GAP);
    if (getStage() != null) {
      getStage().setKeyboardFocus(field);
      field.selectAll();
    }
  }

  private void finishRename(String value) {
    if (!renaming) return;
    renaming = false;
    buildHeaderRow();
    if (value != null && !value.equals(label)) onRename.accept(value);
  }

  private void buildHeaderRow() {
    headerRow.clearChildren();
    ImageButton rename = Scene2dElementFactory.createIconButton("hud/pencil.png", "default");
    rename.pad(ICON_BUTTON_PAD);
    rename.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            startRename();
          }
        });
    headerRow.add(header).growX().minWidth(0f).height(ROW_HEIGHT);
    headerRow.add(rename).size(ROW_HEIGHT).padLeft(GAP);
  }
}
