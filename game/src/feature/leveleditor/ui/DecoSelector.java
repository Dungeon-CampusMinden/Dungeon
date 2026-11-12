package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.utils.Scene2dElementFactory;
import feature.entities.deco.Deco;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog choosing a decoration from a grid of their images, shown above the level editor.
 *
 * <p>It opens scrolled to the current decoration and closes when a decoration is chosen, on Escape
 * or on a click next to it. The header names the decoration under the cursor, or the current one.
 */
public final class DecoSelector {

  private static final int COLUMNS = 7;
  private static final float BUTTON_SIZE = 64f;
  private static final float BUTTON_PADDING = 3f;
  private static final float CELL_SIZE = BUTTON_SIZE + BUTTON_PADDING * 2;
  private static final float ROW_HEIGHT = CELL_SIZE + BUTTON_PADDING * 2;
  private static final float IMAGE_SIZE = 54f;
  private static final float MAX_HEIGHT = 420f;
  // Room for the scroll bar next to the grid.
  private static final float SCROLL_BAR_SPACE = 14f;

  // The panel while the selector is shown.
  private static Actor shown;

  private DecoSelector() {}

  /**
   * Shows the selector.
   *
   * @param choices decorations to choose from, in display order
   * @param selected current decoration, highlighted and scrolled into view; may be {@code null}
   * @param onPick called with the chosen decoration, after the selector closed
   */
  public static void show(List<Deco> choices, Deco selected, Consumer<Deco> onPick) {
    show(choices, false, selected, onPick);
  }

  /**
   * Shows the selector, optionally with a first entry for no decoration.
   *
   * @param choices decorations to choose from, in display order
   * @param optional whether no decoration can be chosen, which is passed as {@code null}
   * @param selected current decoration, highlighted and scrolled into view; {@code null} for none
   * @param onPick called with the chosen decoration, after the selector closed
   */
  public static void show(
      List<Deco> choices, boolean optional, Deco selected, Consumer<Deco> onPick) {
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
    Stage stage = ui == null ? null : ui.getStage();
    List<Deco> entries = new ArrayList<>();
    if (optional) entries.add(null);
    entries.addAll(choices);
    if (stage == null || entries.isEmpty()) return;

    int columns = Math.min(COLUMNS, entries.size());
    int rows = (entries.size() + columns - 1) / columns;
    Label hint = EditorTheme.label("", 13, EditorTheme.TEXT_MUTED);
    hint.setEllipsis(true);
    Runnable showSelected =
        () ->
            hint.setText(
                selected == null && !optional ? choices.size() + " decorations" : name(selected));
    showSelected.run();

    Table grid = new Table();
    grid.top().left();
    for (int index = 0; index < entries.size(); index++) {
      Deco deco = entries.get(index);
      DecoButton button = new DecoButton(deco, BUTTON_SIZE, IMAGE_SIZE, deco == selected);
      button.onClick(
          () -> {
            close();
            onPick.accept(deco);
          });
      button.addListener(
          new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
              if (pointer == -1) hint.setText(name(deco));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
              if (pointer != -1 || (toActor != null && toActor.isDescendantOf(button))) return;
              showSelected.run();
            }
          });
      grid.add(button).size(CELL_SIZE).pad(BUTTON_PADDING);
      if ((index + 1) % columns == 0) grid.row();
    }
    ScrollPane pane = EditorTheme.scrollPane(grid);
    pane.setScrollbarsOnTop(false);

    Table panel = new Table();
    panel.setTouchable(Touchable.enabled);
    panel.setBackground(EditorTheme.panel());
    panel.pad(12f);
    Table header = new Table();
    header.add(EditorTheme.boldLabel("Decorations", 17, EditorTheme.TEXT)).left();
    header.add(hint).left().padLeft(10f).padTop(2f).growX().minWidth(0f);
    ImageButton closeButton =
        EditorTheme.iconButton(EditorTheme.icon("close"), EditorTheme.Variant.GHOST, "Close (Esc)");
    closeButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            close();
          }
        });
    header.add(closeButton).size(28f);
    float width = columns * ROW_HEIGHT + SCROLL_BAR_SPACE;
    panel.add(header).width(width).padBottom(8f).row();
    panel.add(pane).width(width).height(Math.min(MAX_HEIGHT, rows * ROW_HEIGHT));
    panel.pack();
    pane.layout();
    int selectedIndex = selected == null && !optional ? -1 : entries.indexOf(selected);
    if (selectedIndex >= 0) {
      int selectedRow = selectedIndex / columns;
      float scrollY = selectedRow * ROW_HEIGHT - (pane.getHeight() - ROW_HEIGHT) / 2f;
      Scene2dElementFactory.scrollPaneScrollTo(
          pane, 0, Math.max(0f, Math.min(scrollY, pane.getMaxY())));
    }

    shown = panel;
    ui.showModal(
        panel,
        () -> {
          if (shown == panel) shown = null;
        });
    stage.setScrollFocus(pane);
  }

  /** Closes the selector, if it is shown. */
  public static void close() {
    if (shown == null) return;
    shown = null;
    LevelEditorSystem.ui().ifPresent(LevelEditorUI::closeModal);
  }

  /**
   * Returns the name of a decoration with spaces between its words, e.g. "Bookshelf Large".
   *
   * @param deco decoration, or {@code null} for no decoration
   * @return readable name, "None" for no decoration
   */
  public static String name(Deco deco) {
    if (deco == null) return "None";
    String name = deco.name();
    StringBuilder result = new StringBuilder(name.length() + 8);
    for (int index = 0; index < name.length(); index++) {
      char current = name.charAt(index);
      if (index > 0) {
        char previous = name.charAt(index - 1);
        boolean caseBoundary =
            Character.isUpperCase(current)
                && (Character.isLowerCase(previous)
                    || (Character.isUpperCase(previous)
                        && index + 1 < name.length()
                        && Character.isLowerCase(name.charAt(index + 1))));
        boolean numberBoundary =
            (Character.isDigit(current) && Character.isLetter(previous))
                || (Character.isLetter(current) && Character.isDigit(previous));
        if (caseBoundary || numberBoundary) result.append(' ');
      }
      result.append(current);
    }
    return result.toString();
  }
}
