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
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import feature.leveleditor.PrefabCatalog;
import feature.prefabs.Prefab;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Library of all prefabs that can be added to a level, shown in the left panel instead of the
 * prefab list while adding. Prefabs are grouped by category, with the recently added ones on top,
 * and can be searched by name.
 *
 * <p>Prefabs are dragged into the level to place them. Prefabs without a world position, e.g.
 * dialogs or logic, are dropped onto the prefab that triggers them, or clicked to attach them to
 * the selected prefab.
 */
public final class PrefabLibrary extends Table {

  private static final int COLUMNS = 4;
  private static final float TILE_WIDTH = 66f;
  private static final float TILE_HEIGHT = 68f;
  private static final float ICON_SIZE = 30f;
  private static final int MAX_RECENT = 8;

  // Recently added prefab types, newest first. Remembered between mode switches.
  private static final LinkedList<String> RECENT = new LinkedList<>();

  private final Listener listener;
  private final Predicate<Prefab> attached;
  private final DragAndDrop dragAndDrop = new DragAndDrop();
  private final Table content = new Table();
  private final TextField search;
  private final List<PrefabCatalog.Category> categories = PrefabCatalog.typeCategories();

  /** Receives the interactions with the prefabs of the library. */
  public interface Listener {
    /**
     * Called when the user starts dragging a prefab.
     *
     * @param prefab dragged prefab
     */
    void dragStarted(Prefab prefab);

    /**
     * Called when a dragged prefab is released, wherever that is.
     *
     * @param prefab dragged prefab
     */
    void dropped(Prefab prefab);

    /**
     * Called when a prefab is clicked without dragging it.
     *
     * @param prefab clicked prefab
     */
    void clicked(Prefab prefab);

    /** Called when the library should be closed, to show the prefab list again. */
    void close();
  }

  /**
   * Creates a library of all registered prefabs.
   *
   * @param listener receives drags and clicks
   * @param attached whether a prefab has no world position and is attached to other prefabs
   */
  public PrefabLibrary(Listener listener, Predicate<Prefab> attached) {
    this.listener = listener;
    this.attached = attached;
    top();
    defaults().growX().minWidth(0f);
    dragAndDrop.setDragActorPosition(-ICON_SIZE / 2f, ICON_SIZE / 2f);
    dragAndDrop.setTapSquareSize(6f);

    Table header = new Table();
    ImageButton back =
        EditorTheme.iconButton(
            EditorTheme.icon("chevron-left", EditorTheme.TEXT),
            EditorTheme.Variant.GHOST,
            "Back to the prefab list (Esc)");
    back.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            PrefabLibrary.this.listener.close();
          }
        });
    header.add(back).size(28f).padRight(4f);
    header.add(EditorTheme.boldLabel("Add Prefab", 17, EditorTheme.TEXT)).left().growX();
    add(header).padBottom(8f).row();

    search = EditorTheme.textField("", 14);
    search.setMessageText("Search prefabs");
    TextField.TextFieldStyle searchStyle = new TextField.TextFieldStyle(search.getStyle());
    searchStyle.background = EditorTheme.padded(searchStyle.background, 8f, 4f);
    searchStyle.background.setLeftWidth(28f);
    searchStyle.focusedBackground = EditorTheme.padded(searchStyle.focusedBackground, 8f, 4f);
    searchStyle.focusedBackground.setLeftWidth(28f);
    search.setStyle(searchStyle);
    search.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            rebuild();
          }
        });
    search.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != Input.Keys.ESCAPE) return false;
            if (!search.getText().isEmpty()) {
              search.setText("");
              rebuild();
            } else {
              PrefabLibrary.this.listener.close();
            }
            return true;
          }
        });
    Image glass = new Image(EditorTheme.icon("search", EditorTheme.TEXT_MUTED));
    glass.setScaling(Scaling.fit);
    Table overlay = new Table();
    overlay.setTouchable(Touchable.disabled);
    overlay.add(glass).size(14f).padLeft(9f).left().expandX();
    add(new Stack(search, overlay)).height(EditorTheme.CONTROL_HEIGHT).padBottom(6f).row();

    Label hint =
        EditorTheme.label(
            "Drag a prefab into the level. Dialogs, sounds and logic go onto the prefab that"
                + " triggers them: drop them there, or click them to attach them to the"
                + " selection.",
            12,
            EditorTheme.TEXT_MUTED);
    hint.setWrap(true);
    add(hint).padBottom(4f).padLeft(2f).row();

    content.top().left();
    ScrollPane scroll = EditorTheme.scrollPane(content);
    add(scroll).grow().minHeight(0f).row();
    rebuild();
  }

  /**
   * Remembers a prefab as recently added, so it is listed on top.
   *
   * @param type prefab type
   */
  public static void remember(String type) {
    RECENT.remove(type);
    RECENT.addFirst(type);
    while (RECENT.size() > MAX_RECENT) RECENT.removeLast();
  }

  /**
   * Moves the keyboard focus to the search field, also in the next frame: the level editor drops
   * the focus of text fields on clicks outside of them, e.g. on the button that opened the library.
   */
  public void focusSearch() {
    if (getStage() != null) getStage().setKeyboardFocus(search);
    com.badlogic.gdx.Gdx.app.postRunnable(
        () -> {
          if (getStage() != null) getStage().setKeyboardFocus(search);
        });
  }

  private void rebuild() {
    content.clearChildren();
    dragAndDrop.clear();
    String query = search.getText().strip().toLowerCase(Locale.ROOT);
    boolean any = false;
    if (query.isEmpty() && !RECENT.isEmpty()) {
      List<Prefab> recent = new ArrayList<>();
      for (String type : RECENT) {
        for (PrefabCatalog.Category category : categories) {
          for (Prefab prefab : category.prefabs()) {
            if (prefab.type().equals(type)) recent.add(prefab);
          }
        }
      }
      any |= section("Recent", recent);
    }
    for (PrefabCatalog.Category category : categories) {
      List<Prefab> matching = new ArrayList<>();
      for (Prefab prefab : category.prefabs()) {
        String text = (prefab.displayName() + " " + category.name()).toLowerCase(Locale.ROOT);
        if (query.isEmpty() || matchesAll(text, query)) matching.add(prefab);
      }
      any |= section(category.name(), matching);
    }
    if (!any) {
      content
          .add(EditorTheme.label("No prefab matches.", 14, EditorTheme.TEXT_MUTED))
          .pad(8f)
          .left();
    }
  }

  private static boolean matchesAll(String text, String query) {
    for (String term : query.split("\\s+")) {
      if (!text.contains(term)) return false;
    }
    return true;
  }

  private boolean section(String name, List<Prefab> prefabs) {
    if (prefabs.isEmpty()) return false;
    Label caption = EditorTheme.caption(name);
    content.add(caption).colspan(COLUMNS).left().padTop(12f).padBottom(4f).padLeft(2f).row();
    int column = 0;
    for (Prefab prefab : prefabs) {
      content.add(tile(prefab)).size(TILE_WIDTH, TILE_HEIGHT).pad(1f);
      if (++column == COLUMNS) {
        column = 0;
        content.row();
      }
    }
    if (column != 0) content.row();
    return true;
  }

  private Actor tile(Prefab prefab) {
    boolean logic = attached.test(prefab);
    Table tile = new Table();
    tile.setTouchable(Touchable.enabled);
    tile.setUserObject(engine.utils.Cursors.INTERACT);
    tile.setBackground(EditorTheme.rounded(EditorTheme.FIELD, null, 5));
    Actor icon = PrefabIcons.create(prefab.type(), ICON_SIZE);
    tile.add(icon).size(ICON_SIZE).padTop(6f).row();
    Label caption = EditorTheme.label(prefab.displayName(), 11, EditorTheme.TEXT);
    caption.setFontScale(1f);
    caption.setWrap(true);
    caption.setAlignment(Align.center);
    tile.add(caption).width(TILE_WIDTH - 2f).padTop(3f).expandY().top();
    tile.addListener(
        new InputListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer == -1)
              tile.setBackground(EditorTheme.rounded(EditorTheme.BUTTON_OVER, null, 5));
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (pointer != -1 || (toActor != null && toActor.isDescendantOf(tile))) return;
            tile.setBackground(EditorTheme.rounded(EditorTheme.FIELD, null, 5));
          }
        });
    tile.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            listener.clicked(prefab);
          }
        });
    EditorTheme.tooltip(
        tile,
        prefab.displayName()
            + (logic
                ? ": drop it onto the prefab that triggers it, or click to attach it to the"
                    + " selection."
                : ": drag it into the level."));
    dragAndDrop.addSource(
        new DragAndDrop.Source(tile) {
          @Override
          public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
            DragAndDrop.Payload payload = new DragAndDrop.Payload();
            payload.setObject(prefab);
            Actor ghost = PrefabIcons.create(prefab.type(), ICON_SIZE);
            ghost.setSize(ICON_SIZE, ICON_SIZE);
            ghost.getColor().a = 0.85f;
            payload.setDragActor(ghost);
            listener.dragStarted(prefab);
            return payload;
          }

          @Override
          public void dragStop(
              InputEvent event,
              float x,
              float y,
              int pointer,
              DragAndDrop.Payload payload,
              DragAndDrop.Target target) {
            listener.dropped(prefab);
          }
        });
    return tile;
  }
}
