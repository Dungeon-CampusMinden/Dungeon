package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import engine.Game;
import engine.utils.Cursors;
import feature.leveleditor.LevelProblems;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProblem;
import feature.systems.LevelEditorSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel listing the problems of the prefabs of the edited level in detail, opened from the status
 * bar, see {@link LevelProblems}. The problems are grouped like the prefab list, by the group a
 * prefab is listed in.
 *
 * <p>Clicking a problem selects its prefab and centers the camera on it, the panel stays open to go
 * through the problems one by one.
 */
public final class ProblemsPanel extends Table {

  /** Width of the panel. */
  public static final float WIDTH = 460f;

  private static final int FONT_SIZE = 14;
  private static final int MESSAGE_FONT_SIZE = 13;
  private static final float ICON_SIZE = 14f;

  private final Table list = new Table();
  private final ScrollPane scroll;
  private int shownVersion = -1;
  // The problem clicked last, marked in the list.
  private PrefabProblem focused;

  /**
   * Creates the panel.
   *
   * @param close closes the panel
   */
  public ProblemsPanel(Runnable close) {
    setTouchable(Touchable.enabled);
    setBackground(EditorTheme.panel());
    pad(10f, 10f, 8f, 10f);
    top().left();
    defaults().growX().minWidth(0f);

    Table header = new Table();
    header.add(EditorTheme.boldLabel("Problems", 17, EditorTheme.TEXT)).left().growX();
    ImageButton closeButton =
        EditorTheme.iconButton(
            EditorTheme.icon("close", EditorTheme.TEXT_MUTED), EditorTheme.Variant.GHOST, null);
    closeButton.getImageCell().size(12f);
    closeButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            close.run();
          }
        });
    header.add(closeButton).size(26f);
    add(header).padBottom(4f).row();

    list.top().left();
    list.defaults().growX().minWidth(0f);
    scroll = EditorTheme.scrollPane(list);
    add(scroll).grow().minHeight(0f);
  }

  @Override
  public void act(float delta) {
    int version = isVisible() ? LevelProblems.version() : shownVersion;
    if (version != shownVersion) {
      shownVersion = version;
      rebuild();
    }
    super.act(delta);
  }

  private void rebuild() {
    float scrollY = scroll.getScrollY();
    list.clearChildren();
    List<LevelProblems.Section> sections = LevelProblems.sections();
    if (sections.isEmpty()) {
      list.add(EditorTheme.label("No problems found.", FONT_SIZE, EditorTheme.TEXT_MUTED))
          .left()
          .pad(6f, 4f, 6f, 4f)
          .row();
    }
    Map<String, String> types = new HashMap<>();
    Game.currentLevel()
        .ifPresent(
            level -> {
              for (PrefabInstance instance : level.prefabs()) {
                types.putIfAbsent(instance.name(), instance.type());
              }
            });
    boolean first = true;
    for (LevelProblems.Section section : sections) {
      list.add(sectionHeader(section)).padTop(first ? 2f : 10f).padBottom(2f).row();
      first = false;
      for (PrefabProblem problem : section.problems()) {
        list.add(row(problem, types.get(problem.instance()))).padTop(1f).row();
      }
    }
    // Keeps the place in the list while problems come and go.
    scroll.layout();
    scroll.setScrollY(scrollY);
    scroll.updateVisualScroll();
  }

  private static Table sectionHeader(LevelProblems.Section section) {
    Table header = new Table();
    if (section.group() != null) {
      Image folder = new Image(EditorTheme.icon("folder", EditorTheme.FOLDER));
      folder.setScaling(Scaling.fit);
      header.add(folder).size(ICON_SIZE).padRight(7f);
    }
    Label title =
        section.group() == null
            ? EditorTheme.label("No group", FONT_SIZE, EditorTheme.TEXT_MUTED)
            : EditorTheme.label(String.join(" / ", section.path()), FONT_SIZE, EditorTheme.TEXT);
    title.setEllipsis(true);
    header.add(title).growX().minWidth(0f).left();
    header
        .add(
            EditorTheme.label(
                String.valueOf(section.problems().size()), 13, EditorTheme.TEXT_DISABLED))
        .padLeft(8f)
        .padRight(4f);
    header.pad(2f, 4f, 2f, 0f);
    return header;
  }

  private Table row(PrefabProblem problem, String type) {
    boolean error = problem.severity() == PrefabProblem.Severity.ERROR;
    Table row = new Table();
    row.setTouchable(Touchable.enabled);
    row.setUserObject(Cursors.INTERACT);
    row.pad(4f, 6f, 5f, 6f);
    row.setBackground(problem.equals(focused) ? selectedBackground() : null);

    Image severity =
        new Image(
            EditorTheme.icon(
                error ? "error" : "warning", error ? EditorTheme.ERROR : EditorTheme.WARNING));
    severity.setScaling(Scaling.fit);
    row.add(severity).size(13f).top().padTop(3f).padRight(8f);

    Table text = new Table();
    text.defaults().left().growX().minWidth(0f);
    Table title = new Table();
    if (type != null) title.add(PrefabIcons.create(type, ICON_SIZE)).size(ICON_SIZE).padRight(6f);
    Label name = EditorTheme.label(problem.instance(), FONT_SIZE, EditorTheme.TEXT);
    name.setEllipsis(true);
    title.add(name).growX().minWidth(0f).left();
    text.add(title).row();
    Label message = EditorTheme.label(problem.message(), MESSAGE_FONT_SIZE, EditorTheme.TEXT_MUTED);
    message.setWrap(true);
    text.add(message).padTop(1f).row();
    row.add(text).growX().minWidth(0f);

    row.addListener(
        new InputListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer == -1 && !problem.equals(focused)) {
              row.setBackground(EditorTheme.rounded(EditorTheme.ROW_OVER, null, 4));
            }
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (pointer != -1 || (toActor != null && toActor.isDescendantOf(row))) return;
            row.setBackground(problem.equals(focused) ? selectedBackground() : null);
          }
        });
    row.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            focused = problem;
            rebuild();
            LevelEditorSystem.revealPrefab(problem.instance(), problem.isLinkProblem());
          }
        });
    return row;
  }

  private static Drawable selectedBackground() {
    return EditorTheme.rounded(EditorTheme.ROW_SELECTED, null, 4);
  }
}
