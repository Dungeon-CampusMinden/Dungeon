package feature.leveleditor.ui;

import com.badlogic.gdx.math.Vector2;
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
import engine.components.DrawComponent;
import engine.utils.Scene2dElementFactory;
import feature.entities.deco.Deco;
import feature.systems.LevelEditorSystem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Dialog choosing a decoration from a grid of their images, shown above the level editor.
 *
 * <p>The decorations are listed by category, see {@link DecoCategories}. It opens scrolled to the
 * current decoration and closes when a decoration is chosen, on Escape or on a click next to it.
 * The header names the decoration under the cursor, or the current one, and its size in tiles.
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
   * @param choices decorations to choose from, listed by category, see {@link DecoCategories}
   * @param optional whether no decoration can be chosen, which is passed as {@code null}
   * @param selected current decoration, highlighted and scrolled into view; {@code null} for none
   * @param onPick called with the chosen decoration, after the selector closed
   */
  public static void show(
      List<Deco> choices, boolean optional, Deco selected, Consumer<Deco> onPick) {
    LevelEditorUI ui = LevelEditorSystem.ui().orElse(null);
    Stage stage = ui == null ? null : ui.getStage();
    if (stage == null || (choices.isEmpty() && !optional)) return;

    int columns = Math.min(COLUMNS, choices.size() + (optional ? 1 : 0));
    // The header shows the decoration under the cursor, or the current one, with its size.
    Label name = EditorTheme.label("", 13, EditorTheme.TEXT_MUTED);
    name.setEllipsis(true);
    Label size = EditorTheme.label("", 13, EditorTheme.TEXT_DISABLED);
    Consumer<Deco> describe =
        deco -> {
          name.setText(name(deco));
          size.setText(deco == null ? "" : size(deco));
        };
    Runnable describeSelected =
        () -> {
          if (selected != null || optional) {
            describe.accept(selected);
          } else {
            name.setText(choices.size() + " decorations");
            size.setText("");
          }
        };
    describeSelected.run();

    Table grid = new Table();
    grid.top().left();
    DecoButton[] selectedButton = {null};
    Function<Deco, DecoButton> button =
        deco -> {
          DecoButton result = new DecoButton(deco, BUTTON_SIZE, IMAGE_SIZE, deco == selected);
          if (deco == selected) selectedButton[0] = result;
          result.onClick(
              () -> {
                close();
                onPick.accept(deco);
              });
          result.addListener(
              new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor from) {
                  if (pointer == -1) describe.accept(deco);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                  if (pointer != -1 || (toActor != null && toActor.isDescendantOf(result))) return;
                  describeSelected.run();
                }
              });
          return result;
        };
    if (optional) grid.add(button.apply(null)).size(CELL_SIZE).pad(BUTTON_PADDING).row();
    boolean first = !optional;
    for (DecoCategories.Category category : DecoCategories.of(choices)) {
      grid.add(EditorTheme.caption(category.name()))
          .colspan(columns)
          .left()
          .padTop(first ? 2f : 10f)
          .padBottom(2f)
          .padLeft(BUTTON_PADDING + 2f)
          .row();
      first = false;
      int column = 0;
      for (Deco deco : category.decos()) {
        grid.add(button.apply(deco)).size(CELL_SIZE).pad(BUTTON_PADDING);
        if (++column == columns) {
          column = 0;
          grid.row();
        }
      }
      if (column != 0) grid.row();
    }
    ScrollPane pane = EditorTheme.scrollPane(grid);
    pane.setScrollbarsOnTop(false);

    Table panel = new Table();
    panel.setTouchable(Touchable.enabled);
    panel.setBackground(EditorTheme.panel());
    panel.pad(12f);
    Table header = new Table();
    header.add(EditorTheme.boldLabel("Decorations", 17, EditorTheme.TEXT)).left();
    header.add(name).left().padLeft(10f).padTop(2f).minWidth(0f);
    header.add(size).left().padLeft(8f).padTop(2f);
    header.add().growX();
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
    panel.add(pane).width(width).height(Math.min(MAX_HEIGHT, grid.getPrefHeight()));
    panel.pack();
    pane.layout();
    if (selectedButton[0] != null) {
      DecoButton current = selectedButton[0];
      Vector2 position = current.localToAscendantCoordinates(grid, new Vector2());
      float top = grid.getHeight() - position.y - current.getHeight();
      float scrollY = top - (pane.getHeight() - current.getHeight()) / 2f;
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

  // Size of a decoration in the level, in tiles, e.g. "2 × 1".
  private static String size(Deco deco) {
    DrawComponent draw = new DrawComponent(deco.path(), deco.config());
    return tiles(draw.getWidth()) + " × " + tiles(draw.getHeight());
  }

  private static String tiles(float value) {
    return BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString();
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
