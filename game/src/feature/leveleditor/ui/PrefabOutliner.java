package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import engine.utils.Cursors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * List of the prefab instances of a level, in the style of the outliners of common editors: a
 * search field, quick filters for instances that need attention, and a tree of groups.
 *
 * <p>Groups are made by the designer, e.g. for a room or a puzzle. Prefabs without a world
 * position, e.g. dialogs or sound effects, are nested below the prefab that uses them. Further
 * prefabs using them show them as a dimmed reference.
 *
 * <p>Clicking a prefab selects it, double clicking also moves the camera to it. Hovering a prefab
 * or a group highlights it in the level. Dropping a prefab onto a group moves it into the group,
 * along with the prefabs nested below it. Groups, nested prefabs and, in load order, all prefabs
 * are reordered by dragging them. Groups contain further groups, listed before their prefabs: a
 * group dropped onto the middle of another group moves inside of it, onto its upper or lower edge
 * in front of or behind it. Groups are added inside a group, renamed and removed with the buttons
 * that show when they are hovered.
 *
 * <p>While a search text or a filter is active, the matching instances are listed flat with their
 * groups and types.
 */
public final class PrefabOutliner extends Table {

  private static final float INDENT = 14f;
  private static final float ICON = 16f;
  private static final float DISCLOSURE = 14f;
  private static final float INDICATOR_THICKNESS = 2f;
  private static final float AUTO_SCROLL_MARGIN = 30f;
  private static final float AUTO_SCROLL_SPEED = 400f;
  private static final int FONT = 14;
  // Longest time between the two clicks of a double click, as for the default ClickListener.
  private static final long DOUBLE_CLICK_MILLIS = 400L;

  /** One list item. */
  public sealed interface Item permits Entry, Group, Reference {
    /**
     * Returns an identifier that is unique within the list.
     *
     * @return item ID
     */
    String id();

    /**
     * Returns the items listed below this one.
     *
     * @return the items, empty for a reference
     */
    List<Item> children();
  }

  /**
   * State of an instance that the list points out.
   *
   * @param inactive whether the instance is inactive at the start
   * @param detached whether it has no world position and is not linked to a prefab in the world
   * @param problem 0 without problems, 1 for warnings, 2 for errors of its links
   */
  public record Status(boolean inactive, boolean detached, int problem) {}

  /**
   * A prefab instance.
   *
   * @param id unique item ID
   * @param name instance name
   * @param typeName display name of its prefab
   * @param groups group it is listed in and the groups containing it, outermost first, empty
   *     outside of all groups
   * @param icon creates the icon of its prefab
   * @param status state the list points out
   * @param children prefabs nested below it and references to further prefabs it uses
   */
  public record Entry(
      String id,
      String name,
      String typeName,
      List<String> groups,
      Supplier<Actor> icon,
      Status status,
      List<Item> children)
      implements Item {
    /**
     * Creates an entry with an immutable child list.
     *
     * @param id unique item ID
     * @param name instance name
     * @param typeName display name of its prefab
     * @param groups group it is listed in and the groups containing it, outermost first, empty
     *     outside of all groups
     * @param icon creates the icon of its prefab
     * @param status state the list points out
     * @param children prefabs nested below it and references to further prefabs it uses
     */
    public Entry {
      groups = List.copyOf(groups);
      children = List.copyOf(children);
    }
  }

  /**
   * A group of prefabs.
   *
   * @param id unique item ID, see {@link #groupId}
   * @param name group name
   * @param children groups and prefabs directly inside the group, the groups first, in display
   *     order
   */
  public record Group(String id, String name, List<Item> children) implements Item {
    /**
     * Creates a group with an immutable child list.
     *
     * @param id unique item ID, see {@link #groupId}
     * @param name group name
     * @param children groups and prefabs directly inside the group, the groups first, in display
     *     order
     */
    public Group {
      children = List.copyOf(children);
    }
  }

  /**
   * A prefab that is listed elsewhere, shown below a further prefab using it.
   *
   * @param id unique item ID
   * @param name instance name
   * @param icon creates the icon of its prefab
   * @param inactive whether the instance is inactive at the start
   */
  public record Reference(String id, String name, Supplier<Actor> icon, boolean inactive)
      implements Item {
    @Override
    public List<Item> children() {
      return List.of();
    }
  }

  /** Where a dragged group goes, relative to the group it is dropped onto. */
  public enum Placement {
    /** In front of the group, inside the same group. */
    BEFORE,
    /** Inside of the group, behind the groups inside of it. */
    INSIDE,
    /** Behind the group, inside the same group. */
    AFTER
  }

  /** Receives user actions from the list. */
  public interface Listener {
    /**
     * Invoked when an instance is clicked.
     *
     * @param name clicked instance name
     */
    void select(String name);

    /**
     * Invoked when an instance is double clicked, to move the camera to it.
     *
     * @param name instance name
     */
    void frame(String name);

    /**
     * Invoked when the cursor enters or leaves an instance.
     *
     * @param name hovered instance name, or {@code null} when the cursor left
     */
    void hover(String name);

    /**
     * Invoked when the cursor enters or leaves a group.
     *
     * @param names instances of the hovered group, empty when the cursor left
     */
    void hoverGroup(List<String> names);

    /**
     * Invoked when a new group button is clicked, to add an empty group.
     *
     * @param parent group to add it into, or {@code null} to add it outside of all groups
     * @return name of the new group, which is then renamed in place, or {@code null}
     */
    String createGroup(String parent);

    /**
     * Invoked when a group is renamed.
     *
     * @param group current group name
     * @param newName requested name
     * @return whether the group was renamed
     */
    boolean renameGroup(String group, String newName);

    /**
     * Invoked when a group is removed. Its prefabs and groups stay in the level.
     *
     * @param group group name
     */
    void removeGroup(String group);

    /**
     * Invoked when a group was dropped onto another group.
     *
     * @param group dropped group
     * @param target group it was dropped onto
     * @param placement where it goes relative to the target
     */
    void moveGroup(String group, String target, Placement placement);

    /**
     * Invoked when a prefab was dropped into a group or at a new position.
     *
     * @param name instance name
     * @param group group to move it into, or {@code null} for none
     * @param anchor prefab it is placed next to in load order, or {@code null} to keep its place
     * @param after whether it is placed behind the anchor instead of in front of it
     */
    void movePrefab(String name, String group, String anchor, boolean after);

    /** Invoked when the order button is clicked, to switch between alphabetical and load order. */
    void toggleOrder();

    /** Invoked when the add button is clicked. */
    void add();
  }

  private enum Filter {
    PROBLEMS("Problems", "Instances with broken or questionable links"),
    INACTIVE("Inactive", "Instances that are inactive at the start"),
    DETACHED("Detached", "Logic and effects not linked to a prefab in the world");

    private final String label;
    private final String tooltip;

    Filter(String label, String tooltip) {
      this.label = label;
      this.tooltip = tooltip;
    }
  }

  private enum LineKind {
    GROUP,
    ENTRY,
    REFERENCE,
    // Shown in an expanded group without prefabs.
    EMPTY,
    // Between the groups and the prefabs outside of all groups.
    SEPARATOR
  }

  /**
   * A row of the tree.
   *
   * @param kind what the row shows
   * @param item shown item, {@code null} for an empty group or the separator
   * @param parentId ID of the group or prefab the row is nested in, {@code null} at the top level
   * @param group group the row is in, {@code null} outside of all groups
   * @param depth indentation level
   * @param actor the row
   */
  private record Line(
      LineKind kind, Item item, String parentId, Group group, int depth, Actor actor) {}

  private record DragData(Line line) {}

  /**
   * Result of dropping a dragged row at the cursor.
   *
   * @param group group a prefab moves into, or {@code null} for none; for a dragged group, the
   *     group it is placed at
   * @param anchor prefab a prefab is placed next to in load order, or {@code null}
   * @param after whether it is placed behind the anchor
   * @param placement where a dragged group goes relative to {@code group}
   * @param highlight row highlighted as the drop target, or {@code null} to show a line
   * @param lineX left end of the line
   * @param lineY height of the line
   * @param lineWidth length of the line
   */
  private record Drop(
      String group,
      String anchor,
      boolean after,
      Placement placement,
      Actor highlight,
      float lineX,
      float lineY,
      float lineWidth) {

    private static Drop line(
        String group, String anchor, boolean after, float x, float y, float w) {
      return new Drop(group, anchor, after, null, null, x, y, w);
    }
  }

  private final Listener listener;
  private final DragAndDrop dragAndDrop = new DragAndDrop();
  private final List<DragAndDrop.Source> sources = new ArrayList<>();
  private final Set<String> expanded = new HashSet<>();
  private final Set<Filter> filters = new HashSet<>();
  private final Table list = new Table();
  private final ScrollPane scroll;
  private final TextField search;
  private final ImageButton clearSearch;
  private final Table filterBar = new Table();
  private final Label count = EditorTheme.label("", 13, EditorTheme.TEXT_MUTED);
  private final ImageButton orderButton;
  private final TextTooltip orderTooltip;
  private final ImageButton addButton;
  private final List<Line> lines = new ArrayList<>();
  private final Image dropIndicator;
  private final Image dropHighlight;
  private final Vector2 tmp = new Vector2();
  private List<Item> items = List.of();
  private String selectedName;
  private boolean loadOrder;
  private Actor pendingScrollTarget;
  private float autoScrollDirection;
  private String hovered;
  // Instance clicked in this list, its selection does not scroll the list.
  private String clickedName;
  // Last single click on an instance, which a second click turns into a double click.
  private String lastClickName;
  private long lastClickMillis;
  // Group to rename in place once the list shows it.
  private String pendingRename;
  // Whether the next update shows the selection, also if it did not change.
  private boolean revealSelection;

  /**
   * Creates an empty prefab list.
   *
   * @param listener receiver of user actions
   */
  public PrefabOutliner(Listener listener) {
    this.listener = Objects.requireNonNull(listener, "listener");
    top();
    defaults().growX().minWidth(0f);

    Table header = new Table();
    header.add(EditorTheme.boldLabel("Prefabs", 17, EditorTheme.TEXT)).left();
    header.add(count).left().padLeft(8f).padTop(2f).growX();
    ImageButton newGroup =
        EditorTheme.iconButton(
            EditorTheme.icon("folder-plus"), EditorTheme.Variant.GHOST, "New group");
    newGroup.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            renameLater(PrefabOutliner.this.listener.createGroup(null));
          }
        });
    header.add(newGroup).size(28f);
    ImageButton expand =
        EditorTheme.iconButton(
            EditorTheme.icon("expand-all"), EditorTheme.Variant.GHOST, "Expand all by one level");
    expand.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            expandLevel();
          }
        });
    header.add(expand).size(28f);
    ImageButton collapse =
        EditorTheme.iconButton(
            EditorTheme.icon("collapse-all"), EditorTheme.Variant.GHOST, "Collapse all");
    collapse.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            expanded.clear();
            rebuild(false);
          }
        });
    header.add(collapse).size(28f);
    orderButton =
        EditorTheme.iconButton(EditorTheme.icon("sort-alpha"), EditorTheme.Variant.GHOST, null);
    orderTooltip = EditorTheme.tooltip(orderButton, "");
    orderButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            PrefabOutliner.this.listener.toggleOrder();
          }
        });
    header.add(orderButton).size(28f).padRight(4f);
    addButton =
        EditorTheme.iconButton(
            EditorTheme.icon("plus"), EditorTheme.Variant.PRIMARY, "Add a prefab to the level");
    addButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            PrefabOutliner.this.listener.add();
          }
        });
    header.add(addButton).size(28f);
    add(header).padBottom(8f).row();

    search = EditorTheme.textField("", FONT);
    search.setMessageText("Search name, type or group  (Ctrl+F)");
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
            rebuild(false);
          }
        });
    search.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != Input.Keys.ESCAPE) return false;
            search.setText("");
            if (search.getStage() != null) search.getStage().setKeyboardFocus(null);
            rebuild(false);
            return true;
          }
        });
    Image glass = new Image(EditorTheme.icon("search", EditorTheme.TEXT_MUTED));
    glass.setScaling(Scaling.fit);
    glass.setTouchable(Touchable.disabled);
    clearSearch =
        EditorTheme.iconButton(
            EditorTheme.icon("close", EditorTheme.TEXT_MUTED), EditorTheme.Variant.GHOST, null);
    clearSearch.getImageCell().size(12f);
    clearSearch.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            search.setText("");
            rebuild(false);
          }
        });
    Table searchOverlay = new Table();
    searchOverlay.setTouchable(Touchable.childrenOnly);
    searchOverlay.add(glass).size(14f).padLeft(9f).left();
    searchOverlay.add().growX();
    searchOverlay.add(clearSearch).size(22f).padRight(4f);
    Stack searchStack = new Stack(search, searchOverlay);
    add(searchStack).height(EditorTheme.CONTROL_HEIGHT).padBottom(6f).row();

    add(filterBar).left().padBottom(4f).row();

    list.top();
    list.defaults().growX().minWidth(0f);
    scroll = EditorTheme.scrollPane(list);
    add(scroll).grow().minHeight(0f).row();

    dropIndicator = new Image(EditorTheme.flat(EditorTheme.ACCENT));
    dropIndicator.setTouchable(Touchable.disabled);
    dropHighlight =
        new Image(
            EditorTheme.rounded(
                EditorTheme.alpha(EditorTheme.ACCENT, 0.18f), EditorTheme.ACCENT, 4));
    dropHighlight.setTouchable(Touchable.disabled);
    dragAndDrop.setDragTime(150);
    dragAndDrop.addTarget(
        new DragAndDrop.Target(list) {
          @Override
          public boolean drag(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            updateAutoScroll(y);
            Drop drop = dropAt((DragData) payload.getObject(), y);
            showDrop(drop);
            return drop != null;
          }

          @Override
          public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
            showDrop(null);
            autoScrollDirection = 0f;
          }

          @Override
          public void drop(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            DragData data = (DragData) payload.getObject();
            Drop drop = dropAt(data, y);
            if (drop == null) return;
            if (data.line().item() instanceof Group group) {
              // Shows where the group went.
              if (drop.placement() == Placement.INSIDE) expanded.add(groupId(drop.group()));
              PrefabOutliner.this.listener.moveGroup(group.name(), drop.group(), drop.placement());
            } else if (data.line().item() instanceof Entry entry) {
              PrefabOutliner.this.listener.movePrefab(
                  entry.name(), drop.group(), drop.anchor(), drop.after());
            }
          }
        });
  }

  /** Moves the keyboard focus to the search field and selects its text. */
  public void focusSearch() {
    if (getStage() == null) return;
    getStage().setKeyboardFocus(search);
    search.selectAll();
  }

  /**
   * Returns the button that adds prefabs, to show whether adding is active.
   *
   * @return the add button
   */
  public ImageButton addButton() {
    return addButton;
  }

  /**
   * Renames a group in place as soon as the list shows it, e.g. a group that was just added. A
   * search or filter is cleared, so the list shows the groups.
   *
   * @param group group name, or {@code null} to do nothing
   */
  public void renameLater(String group) {
    if (group == null) return;
    pendingRename = group;
    boolean searching = !search.getText().isEmpty() || !filters.isEmpty();
    search.setText("");
    filters.clear();
    if (expandTo(groupId(group)) || searching) rebuild(false);
  }

  /** Shows the selection with the next update of the list, e.g. after it moved to another group. */
  public void revealLater() {
    revealSelection = true;
  }

  /**
   * Returns the ID of the item of a group.
   *
   * @param group group name
   * @return item ID
   */
  public static String groupId(String group) {
    return "group:" + group;
  }

  /**
   * Rebuilds the list. When the selection changed outside of the list, everything containing the
   * new selection is expanded and the selected entry is scrolled into view.
   *
   * @param items top-level items in display order
   * @param selectedName selected instance name, or {@code null}
   * @param loadOrder whether the items are in load order, which allows reordering them
   */
  public void setItems(List<Item> items, String selectedName, boolean loadOrder) {
    boolean selectionChanged = !Objects.equals(selectedName, this.selectedName);
    // A row clicked in this list is in view already, so the list stays as it is.
    boolean selectedHere = selectionChanged && Objects.equals(selectedName, clickedName);
    if (selectionChanged) clickedName = null;
    boolean orderChanged = loadOrder != this.loadOrder;
    this.items = List.copyOf(items);
    this.selectedName = selectedName;
    this.loadOrder = loadOrder;
    Set<String> expandable = new HashSet<>();
    collectExpandable(this.items, expandable);
    expanded.retainAll(expandable);
    if (pendingRename != null) expandTo(groupId(pendingRename));
    boolean reveal =
        (revealSelection || (selectionChanged && !selectedHere)) && selectedName != null;
    revealSelection = false;
    if (reveal) {
      List<String> path = pathTo(this.items, entryNamed(selectedName));
      if (path != null) expanded.addAll(path);
    }
    ImageButton.ImageButtonStyle orderStyle =
        new ImageButton.ImageButtonStyle(orderButton.getStyle());
    orderStyle.imageUp = EditorTheme.icon(loadOrder ? "sort-order" : "sort-alpha");
    orderButton.setStyle(orderStyle);
    orderTooltip
        .getActor()
        .setText(
            loadOrder
                ? "Load order: drag prefabs to change the order they are created in. Click to"
                    + " sort A-Z."
                : "Sorted A-Z. Click to show the load order, which can be changed by dragging.");
    rebuild(reveal || orderChanged);
  }

  private static void collectExpandable(List<Item> items, Set<String> ids) {
    for (Item item : items) {
      if (item instanceof Group || !item.children().isEmpty()) ids.add(item.id());
      collectExpandable(item.children(), ids);
    }
  }

  /* Expands an item and everything containing it, returns whether the item is in the list. */
  private boolean expandTo(String id) {
    List<String> path = pathTo(items, item -> item.id().equals(id));
    if (path == null) return false;
    expanded.addAll(path);
    expanded.add(id);
    return true;
  }

  /* IDs of the items containing the first item that matches, outermost first; null if none does. */
  private static List<String> pathTo(List<Item> items, Predicate<Item> target) {
    for (Item item : items) {
      if (target.test(item)) return new ArrayList<>();
      List<String> path = pathTo(item.children(), target);
      if (path != null) {
        path.addFirst(item.id());
        return path;
      }
    }
    return null;
  }

  private static Predicate<Item> entryNamed(String name) {
    return item -> item instanceof Entry entry && entry.name().equals(name);
  }

  private static boolean containsEntry(List<Item> items, String name) {
    return name != null && pathTo(items, entryNamed(name)) != null;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (autoScrollDirection != 0f && dragAndDrop.isDragging()) {
      scroll.setScrollY(scroll.getScrollY() + autoScrollDirection * AUTO_SCROLL_SPEED * delta);
    }
    if (pendingScrollTarget != null && getStage() != null) {
      Actor target = pendingScrollTarget;
      pendingScrollTarget = null;
      scroll.validate();
      if (target.isDescendantOf(list)) {
        Vector2 position = target.localToAscendantCoordinates(list, new Vector2());
        scroll.scrollTo(position.x, position.y, target.getWidth(), target.getHeight(), false, true);
      }
    }
  }

  private void rebuild(boolean scrollToSelection) {
    for (DragAndDrop.Source source : sources) dragAndDrop.removeSource(source);
    sources.clear();
    list.clearChildren();
    lines.clear();
    pendingScrollTarget = null;
    clearSearch.setVisible(!search.getText().isEmpty());
    // The rows under the cursor are replaced, they report hovering again when the cursor moves.
    if (hovered != null) {
      hovered = null;
      listener.hover(null);
    }
    listener.hoverGroup(List.of());

    List<Entry> all = entries(items);
    count.setText(String.valueOf(all.size()));
    rebuildFilters(all);

    String query = search.getText().strip().toLowerCase(Locale.ROOT);
    if (!query.isEmpty() || !filters.isEmpty()) {
      pendingRename = null;
      List<Entry> matches = new ArrayList<>();
      for (Entry entry : all) {
        if (matches(entry, query)) matches.add(entry);
      }
      for (Entry entry : matches) {
        Actor row = flatRow(entry);
        list.add(row).height(EditorTheme.ROW_HEIGHT).row();
        if (entry.name().equals(selectedName)) pendingScrollTarget = row;
      }
      if (matches.isEmpty()) {
        Label empty = EditorTheme.label("No prefab matches.", 14, EditorTheme.TEXT_MUTED);
        list.add(empty).pad(10f, 8f, 0f, 8f).left().row();
      }
    } else {
      boolean afterGroups = false;
      for (Item item : items) {
        if (afterGroups && !(item instanceof Group)) {
          addSeparator();
          afterGroups = false;
        }
        addLines(item, null, null, 0, false);
        if (item instanceof Group) afterGroups = true;
      }
      if (items.isEmpty()) {
        Label empty =
            EditorTheme.label(
                "No prefabs in this level yet.\nClick + to add one.", 14, EditorTheme.TEXT_MUTED);
        empty.setWrap(true);
        list.add(empty).pad(10f, 8f, 0f, 8f).growX().row();
      }
    }
    if (!scrollToSelection) pendingScrollTarget = null;
    if (pendingRename != null) {
      String group = pendingRename;
      for (Line line : lines) {
        if (line.item() instanceof Group shown
            && shown.name().equals(group)
            && line.actor() instanceof GroupRow row) {
          pendingRename = null;
          row.startRename();
          pendingScrollTarget = row;
        }
      }
    }
  }

  private void addLines(Item item, String parentId, Group group, int depth, boolean inDetached) {
    switch (item) {
      case Group shown -> {
        boolean open = expanded.contains(shown.id());
        GroupRow row = new GroupRow(shown, depth, open);
        addLine(new Line(LineKind.GROUP, shown, parentId, shown, depth, row));
        addSource(row, shown.name());
        if (!open) return;
        if (shown.children().isEmpty()) {
          Table empty = new Table();
          empty
              .add(EditorTheme.label("Empty", 13, EditorTheme.TEXT_DISABLED))
              .padLeft(6f + (depth + 1) * INDENT + DISCLOSURE)
              .left()
              .growX();
          addLine(new Line(LineKind.EMPTY, null, shown.id(), shown, depth + 1, empty));
        }
        for (Item child : shown.children()) addLines(child, shown.id(), shown, depth + 1, false);
      }
      case Entry entry -> {
        boolean open = !entry.children().isEmpty() && expanded.contains(entry.id());
        Row row = entryRow(entry, depth, open, inDetached);
        addLine(new Line(LineKind.ENTRY, entry, parentId, group, depth, row));
        addSource(row, entry.name());
        if (entry.name().equals(selectedName)) pendingScrollTarget = row;
        if (!open) return;
        boolean detached = inDetached || entry.status().detached();
        for (Item child : entry.children()) {
          addLines(child, entry.id(), group, depth + 1, detached);
        }
      }
      case Reference reference ->
          addLine(
              new Line(
                  LineKind.REFERENCE,
                  reference,
                  parentId,
                  group,
                  depth,
                  referenceRow(reference, depth)));
    }
  }

  private void addLine(Line line) {
    lines.add(line);
    list.add(line.actor()).height(EditorTheme.ROW_HEIGHT).row();
  }

  private void addSeparator() {
    Table separator = new Table();
    separator.add(new Image(EditorTheme.flat(EditorTheme.LINE))).height(1f).growX().padLeft(6f);
    lines.add(new Line(LineKind.SEPARATOR, null, null, null, 0, separator));
    list.add(separator).height(9f).row();
  }

  /* The prefabs among the items and below them, depth first. */
  private static List<Entry> entries(List<Item> items) {
    List<Entry> result = new ArrayList<>();
    for (Item item : items) {
      if (item instanceof Entry entry) result.add(entry);
      result.addAll(entries(item.children()));
    }
    return result;
  }

  private boolean matches(Entry entry, String query) {
    for (Filter filter : filters) {
      boolean passes =
          switch (filter) {
            case PROBLEMS -> entry.status().problem() > 0;
            case INACTIVE -> entry.status().inactive();
            case DETACHED -> entry.status().detached();
          };
      if (!passes) return false;
    }
    if (query.isEmpty()) return true;
    String groups = String.join(" ", entry.groups());
    String text = (entry.name() + " " + entry.typeName() + " " + groups).toLowerCase(Locale.ROOT);
    for (String term : query.split("\\s+")) {
      if (!text.contains(term)) return false;
    }
    return true;
  }

  private void rebuildFilters(List<Entry> all) {
    filterBar.clearChildren();
    Map<Filter, Integer> counts = new HashMap<>();
    for (Entry entry : all) {
      if (entry.status().problem() > 0) counts.merge(Filter.PROBLEMS, 1, Integer::sum);
      if (entry.status().inactive()) counts.merge(Filter.INACTIVE, 1, Integer::sum);
      if (entry.status().detached()) counts.merge(Filter.DETACHED, 1, Integer::sum);
    }
    filters.removeIf(filter -> !counts.containsKey(filter));
    for (Filter filter : Filter.values()) {
      Integer amount = counts.get(filter);
      if (amount == null) continue;
      boolean on = filters.contains(filter);
      TextButton chip =
          EditorTheme.button(
              filter.label + "  " + amount,
              on ? EditorTheme.Variant.ACTIVE : EditorTheme.Variant.DEFAULT,
              12);
      if (filter == Filter.PROBLEMS && !on) {
        chip.getLabel().setColor(EditorTheme.WARNING);
      }
      chip.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              if (!filters.remove(filter)) filters.add(filter);
              rebuild(false);
            }
          });
      EditorTheme.tooltip(chip, filter.tooltip);
      filterBar.add(chip).height(24f).padRight(4f);
    }
  }

  /* A search result: the entry with its group and type. */
  private Row flatRow(Entry entry) {
    boolean selected = entry.name().equals(selectedName);
    Row row = new Row(selected ? Highlight.SELECTED : Highlight.NONE, entry.name());
    row.padLeft(6f);
    row.add(entry.icon().get()).size(ICON).padRight(7f);
    Label name = EditorTheme.label(entry.name(), FONT, nameColor(selected, entry.status()));
    name.setEllipsis(true);
    row.add(name).growX().minWidth(0f);
    // Group names are unique, the innermost one is enough to find the prefab in the tree.
    String context =
        entry.groups().isEmpty()
            ? entry.typeName()
            : entry.groups().getLast() + "  ·  " + entry.typeName();
    Label type = EditorTheme.label(context, 12, EditorTheme.TEXT_DISABLED);
    type.setEllipsis(true);
    row.add(type).padLeft(6f).minWidth(0f).maxWidth(140f);
    addStatus(row, entry.status(), true);
    row.padRight(6f);
    addSelectListener(row, entry.name(), null, null);
    return row;
  }

  private Row entryRow(Entry entry, int depth, boolean open, boolean inDetached) {
    boolean selected = entry.name().equals(selectedName);
    Row row = new Row(selected ? Highlight.SELECTED : Highlight.NONE, entry.name());
    if (!open && containsEntry(entry.children(), selectedName)) row.base(hiddenSelection());
    row.padLeft(6f + depth * INDENT);
    boolean expandable = !entry.children().isEmpty();
    Actor disclosure = disclosure(expandable, open);
    row.add(disclosure).width(DISCLOSURE).fillY();
    row.add(entry.icon().get()).size(ICON).padRight(7f);
    Label name = EditorTheme.label(entry.name(), FONT, nameColor(selected, entry.status()));
    name.setEllipsis(true);
    row.add(name).growX().minWidth(0f);
    // Below a detached prefab, the prefabs nested in it are detached as well.
    addStatus(row, entry.status(), !inDetached);
    row.padRight(6f);
    addSelectListener(row, entry.name(), expandable ? disclosure : null, entry.id());
    return row;
  }

  private Row referenceRow(Reference reference, int depth) {
    boolean selected = reference.name().equals(selectedName);
    Row row = new Row(selected ? Highlight.REFERENCED : Highlight.NONE, reference.name());
    row.padLeft(6f + depth * INDENT);
    Image arrow = new Image(EditorTheme.icon("reference", EditorTheme.TEXT_DISABLED));
    arrow.setScaling(Scaling.fit);
    EditorTheme.tooltip(arrow, "Used here too, listed elsewhere");
    row.add(arrow).size(10f).padRight(DISCLOSURE - 10f);
    Actor icon = reference.icon().get();
    icon.getColor().a = 0.55f;
    row.add(icon).size(ICON).padRight(7f);
    Color color =
        selected
            ? EditorTheme.alpha(EditorTheme.SELECTION, 0.8f)
            : reference.inactive() ? EditorTheme.TEXT_DISABLED : EditorTheme.TEXT_MUTED;
    Label name = EditorTheme.label(reference.name(), FONT, color);
    name.setEllipsis(true);
    row.add(name).growX().minWidth(0f);
    row.padRight(6f);
    addSelectListener(row, reference.name(), null, null);
    return row;
  }

  private static Color nameColor(boolean selected, Status status) {
    if (selected) return EditorTheme.SELECTION;
    return status.inactive() ? EditorTheme.TEXT_DISABLED : EditorTheme.TEXT;
  }

  /* The arrow that expands a row, or an empty space of the same size. */
  private static Actor disclosure(boolean expandable, boolean open) {
    Container<Image> container = new Container<>();
    container.setTouchable(Touchable.enabled);
    if (expandable) {
      Image chevron =
          new Image(
              EditorTheme.icon(open ? "chevron-down" : "chevron-right", EditorTheme.TEXT_MUTED));
      chevron.setScaling(Scaling.fit);
      container.setActor(chevron);
      container.size(9f);
      container.padRight(5f);
    }
    return container;
  }

  /*
   * Clicking a row selects its instance, a double click also moves the camera to it. Clicking the
   * arrow of an expandable row, or the indentation left of it, expands or collapses it instead.
   */
  private void addSelectListener(Row row, String name, Actor disclosure, String id) {
    row.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (disclosure != null && x <= disclosure.getX() + disclosure.getWidth()) {
              toggle(id);
              return;
            }
            if (isDoubleClick(name)) {
              listener.frame(name);
            } else {
              clickedName = name;
              listener.select(name);
            }
          }
        });
  }

  /**
   * Returns whether a click on an instance follows a click on the same instance quickly enough to
   * be a double click. The clicks are counted here and not by the click listener of the row,
   * because a click that changes the selection rebuilds the rows.
   *
   * @param name clicked instance name
   * @return true for the second click of a double click
   */
  private boolean isDoubleClick(String name) {
    long now = TimeUtils.millis();
    boolean second = name.equals(lastClickName) && now - lastClickMillis <= DOUBLE_CLICK_MILLIS;
    lastClickName = second ? null : name;
    lastClickMillis = now;
    return second;
  }

  private static void addStatus(Table row, Status status, boolean showDetached) {
    if (status.detached() && showDetached) {
      Image icon = new Image(EditorTheme.icon("link", EditorTheme.TEXT_DISABLED));
      icon.setScaling(Scaling.fit);
      EditorTheme.tooltip(
          icon, "Not linked to a prefab in the world, so it is not shown in the level.");
      row.add(icon).size(13f).padLeft(5f);
    }
    if (status.inactive()) {
      Image icon = new Image(EditorTheme.icon("power", EditorTheme.TEXT_DISABLED));
      icon.setScaling(Scaling.fit);
      EditorTheme.tooltip(icon, "Inactive at the start, until an Activate action runs.");
      row.add(icon).size(13f).padLeft(5f);
    }
    if (status.problem() > 0) {
      boolean error = status.problem() > 1;
      Image icon =
          new Image(
              EditorTheme.icon(
                  error ? "error" : "warning", error ? EditorTheme.ERROR : EditorTheme.WARNING));
      icon.setScaling(Scaling.fit);
      EditorTheme.tooltip(
          icon, error ? "A link is broken, see Links." : "A link needs attention, see Links.");
      row.add(icon).size(14f).padLeft(5f);
    }
  }

  /* Background of a collapsed row that contains the selection. */
  private static Drawable hiddenSelection() {
    return EditorTheme.barred(new Color(1f, 0.76f, 0.24f, 0.07f), EditorTheme.SELECTION);
  }

  /**
   * Expands the rows of the topmost level that is not fully expanded yet: the first click expands
   * all groups, the next one the prefabs inside of them, and so on.
   */
  private void expandLevel() {
    List<List<String>> levels = new ArrayList<>();
    collectLevels(items, 0, levels);
    for (List<String> level : levels) {
      if (expanded.containsAll(level)) continue;
      expanded.addAll(level);
      break;
    }
    rebuild(false);
  }

  private static void collectLevels(List<Item> items, int depth, List<List<String>> levels) {
    for (Item item : items) {
      List<Item> children = item.children();
      if (children.isEmpty() && !(item instanceof Group)) continue;
      while (levels.size() <= depth) levels.add(new ArrayList<>());
      levels.get(depth).add(item.id());
      collectLevels(children, depth + 1, levels);
    }
  }

  private void toggle(String id) {
    if (!expanded.remove(id)) expanded.add(id);
    rebuild(false);
  }

  private void addSource(Actor handle, String label) {
    Line line = lines.getLast();
    if (line.actor() != handle) return;
    DragData data = new DragData(line);
    DragAndDrop.Source source =
        new DragAndDrop.Source(handle) {
          @Override
          public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
            DragAndDrop.Payload payload = new DragAndDrop.Payload();
            payload.setObject(data);
            Table ghost = new Table();
            ghost.setBackground(
                EditorTheme.padded(
                    EditorTheme.rounded(EditorTheme.BUTTON_OVER, EditorTheme.ACCENT, 4), 8f, 2f));
            ghost.add(EditorTheme.label(label, FONT, EditorTheme.TEXT));
            ghost.pack();
            ghost.getColor().a = 0.9f;
            ghost.setTouchable(Touchable.disabled);
            payload.setDragActor(ghost);
            dragAndDrop.setDragActorPosition(-x, -y + ghost.getHeight());
            return payload;
          }
        };
    dragAndDrop.addSource(source);
    sources.add(source);
  }

  /**
   * Decides what dropping a dragged row at a height of the list does.
   *
   * @param data the dragged row
   * @param y height in the list
   * @return the drop, or {@code null} if dropping there changes nothing
   */
  private Drop dropAt(DragData data, float y) {
    int dragged = lines.indexOf(data.line());
    if (dragged < 0) return null;
    int index = lineAt(y);
    return data.line().item() instanceof Group
        ? groupDrop(dragged, index, y)
        : prefabDrop(dragged, index, y);
  }

  /* Index of the line at a height of the list, -1 below the last line. */
  private int lineAt(float y) {
    for (int index = 0; index < lines.size(); index++) {
      if (y >= bottom(lines.get(index).actor())) return index;
    }
    return -1;
  }

  private boolean upperHalf(int index, float y) {
    Actor actor = lines.get(index).actor();
    return y >= bottom(actor) + actor.getHeight() / 2f;
  }

  /*
   * Moves a group: onto the middle of a group inside of it, onto its upper or lower edge in front
   * of or behind it. Onto a prefab, it moves into the group listing the prefab, below all groups
   * behind the last group.
   */
  private Drop groupDrop(int dragged, int index, float y) {
    if (index >= dragged && index <= lastDescendant(dragged)) return null;
    Line source = lines.get(dragged);
    Line target = index < 0 ? null : lines.get(index);
    if (target == null || target.group() == null) return behindLastGroup(source);
    if (target.kind() != LineKind.GROUP) {
      return groupDropAt(source, headerOf(target.group()), Placement.INSIDE);
    }
    Actor actor = target.actor();
    float share = (y - bottom(actor)) / actor.getHeight();
    // The lower edge of an expanded group is above the first row inside of it.
    boolean open = expanded.contains(target.group().id());
    Placement placement =
        share > 0.7f
            ? Placement.BEFORE
            : share < 0.3f && !open ? Placement.AFTER : Placement.INSIDE;
    return groupDropAt(source, target, placement);
  }

  private Drop behindLastGroup(Line source) {
    Line last = null;
    for (Line line : lines) {
      if (line.kind() == LineKind.GROUP && line.parentId() == null) last = line;
    }
    return groupDropAt(source, last, Placement.AFTER);
  }

  /* A group dropped at another group, or null if that changes nothing. */
  private Drop groupDropAt(Line source, Line target, Placement placement) {
    if (target == null) return null;
    String moving = source.item().id();
    Group group = (Group) target.item();
    if (group.id().equals(moving)) return null;
    if (placement == Placement.INSIDE) {
      if (group.id().equals(source.parentId())) return null;
      return new Drop(group.name(), null, false, placement, target.actor(), 0f, 0f, 0f);
    }
    List<Group> siblings = siblingGroups(target.parentId());
    int neighbour = siblings.indexOf(group) + (placement == Placement.BEFORE ? -1 : 1);
    if (neighbour >= 0
        && neighbour < siblings.size()
        && siblings.get(neighbour).id().equals(moving)) {
      return null;
    }
    int at = lines.indexOf(target);
    Actor edge = lines.get(placement == Placement.BEFORE ? at : lastDescendant(at)).actor();
    float lineX = 6f + target.depth() * INDENT;
    float lineY = placement == Placement.BEFORE ? top(edge) : bottom(edge);
    return new Drop(
        group.name(), null, false, placement, null, lineX, lineY, edge.getWidth() - lineX);
  }

  /* The groups directly inside a group, or outside of all groups for a null ID. */
  private List<Group> siblingGroups(String parentId) {
    List<Item> children = items;
    if (parentId != null) {
      Line parent = null;
      for (Line line : lines) {
        if (line.kind() == LineKind.GROUP && line.item().id().equals(parentId)) parent = line;
      }
      children = parent == null ? List.of() : ((Group) parent.item()).children();
    }
    List<Group> result = new ArrayList<>();
    for (Item item : children) {
      if (item instanceof Group group) result.add(group);
    }
    return result;
  }

  /* The row of the header of a group. */
  private Line headerOf(Group group) {
    for (Line line : lines) {
      if (line.kind() == LineKind.GROUP && line.item() == group) return line;
    }
    return null;
  }

  /*
   * Moves a prefab: onto a group into the group, between prefabs next to them. Prefabs directly
   * inside a group or outside of all groups are placed in load order only, prefabs nested below
   * another prefab between their siblings always.
   */
  private Drop prefabDrop(int dragged, int index, float y) {
    Line source = lines.get(dragged);
    String name = ((Entry) source.item()).name();
    String currentGroup = source.group() == null ? null : source.group().name();
    // A nested prefab without a group of its own stays with the prefab it is nested below.
    boolean nested = !isDirect(source);
    if (index < 0) {
      if (currentGroup == null || nested) return null;
      Actor last = lines.getLast().actor();
      return Drop.line(null, null, false, 0f, bottom(last), last.getWidth());
    }
    if (index >= dragged && index <= lastDescendant(dragged)) return null;
    Line target = lines.get(index);
    String targetGroup = target.group() == null ? null : target.group().name();
    boolean changesGroup = !Objects.equals(targetGroup, currentGroup);
    boolean siblings = Objects.equals(target.parentId(), source.parentId());
    switch (target.kind()) {
      case GROUP, EMPTY -> {
        return changesGroup ? intoGroup(target.group(), target) : null;
      }
      case SEPARATOR -> {
        if (currentGroup == null || nested) return null;
        Actor actor = target.actor();
        float middle = bottom(actor) + actor.getHeight() / 2f;
        return Drop.line(null, null, false, 0f, middle, actor.getWidth());
      }
      case ENTRY -> {
        boolean direct = isDirect(target);
        if ((siblings && !direct) || (direct && loadOrder && (changesGroup || !nested))) {
          String anchor = ((Entry) target.item()).name();
          if (anchor.equals(name) || (targetGroup == null && nested && changesGroup)) return null;
          boolean after = !upperHalf(index, y);
          Actor edge = lines.get(after ? lastDescendant(index) : index).actor();
          float lineX = 6f + target.depth() * INDENT;
          float lineY = after ? bottom(edge) : top(edge);
          return Drop.line(targetGroup, anchor, after, lineX, lineY, edge.getWidth() - lineX);
        }
        if (!changesGroup || (targetGroup == null && nested)) return null;
        return intoGroup(target.group(), target);
      }
      case REFERENCE -> {
        if (!changesGroup || (targetGroup == null && nested)) return null;
        return intoGroup(target.group(), target);
      }
    }
    return null;
  }

  /* Whether a row is directly inside its group, or at the top level outside of all groups. */
  private static boolean isDirect(Line line) {
    return line.parentId() == null
        || (line.group() != null && line.parentId().equals(line.group().id()));
  }

  /* A drop into a group, shown on its header, or outside of all groups below a row. */
  private Drop intoGroup(Group group, Line near) {
    if (group == null) {
      Actor actor = near.actor();
      return Drop.line(null, null, false, 0f, bottom(actor), actor.getWidth());
    }
    Line header = headerOf(group);
    return header == null
        ? null
        : new Drop(group.name(), null, false, null, header.actor(), 0f, 0f, 0f);
  }

  /* Index of the last row nested below a row, or the row itself. */
  private int lastDescendant(int index) {
    int depth = lines.get(index).depth();
    int last = index;
    while (last + 1 < lines.size()
        && lines.get(last + 1).depth() > depth
        && lines.get(last + 1).kind() != LineKind.SEPARATOR) {
      last++;
    }
    return last;
  }

  private float bottom(Actor actor) {
    return actor.localToAscendantCoordinates(list, tmp.set(0f, 0f)).y;
  }

  private float top(Actor actor) {
    return bottom(actor) + actor.getHeight();
  }

  private void showDrop(Drop drop) {
    dropIndicator.remove();
    dropHighlight.remove();
    if (drop == null) return;
    if (drop.highlight() != null) {
      Actor header = drop.highlight();
      Vector2 position = header.localToAscendantCoordinates(list, tmp.set(0f, 0f));
      list.addActor(dropHighlight);
      dropHighlight.toFront();
      dropHighlight.setBounds(position.x, position.y, header.getWidth(), header.getHeight());
      return;
    }
    list.addActor(dropIndicator);
    dropIndicator.toFront();
    dropIndicator.setBounds(
        drop.lineX(),
        drop.lineY() - INDICATOR_THICKNESS / 2f,
        drop.lineWidth(),
        INDICATOR_THICKNESS);
  }

  private void updateAutoScroll(float y) {
    autoScrollDirection = 0f;
    Vector2 inPane = list.localToAscendantCoordinates(scroll, tmp.set(0f, y));
    if (inPane.y > scroll.getHeight() - AUTO_SCROLL_MARGIN) autoScrollDirection = -1f;
    else if (inPane.y < AUTO_SCROLL_MARGIN) autoScrollDirection = 1f;
  }

  private void hover(String name, boolean entered) {
    if (entered) {
      hovered = name;
      listener.hover(name);
    } else if (Objects.equals(hovered, name)) {
      hovered = null;
      listener.hover(null);
    }
  }

  /** How a row shows the selection. */
  private enum Highlight {
    NONE,
    // The selected prefab.
    SELECTED,
    // A reference to the selected prefab.
    REFERENCED
  }

  /** A clickable row with hover and selection backgrounds. */
  private class Row extends Table {

    private final Highlight highlight;
    private Drawable base;
    private boolean over;

    private Row(Highlight highlight, String hoverName) {
      this.highlight = highlight;
      setTouchable(Touchable.enabled);
      setUserObject(Cursors.INTERACT);
      updateBackground();
      addListener(
          new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
              if (pointer != -1 || (fromActor != null && fromActor.isDescendantOf(Row.this))) {
                return;
              }
              over = true;
              updateBackground();
              if (hoverName != null) hover(hoverName, true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
              if (pointer != -1 || (toActor != null && toActor.isDescendantOf(Row.this))) return;
              over = false;
              updateBackground();
              if (hoverName != null) hover(hoverName, false);
            }
          });
    }

    private void updateBackground() {
      switch (highlight) {
        case SELECTED ->
            setBackground(EditorTheme.barred(EditorTheme.ROW_SELECTED, EditorTheme.SELECTION));
        case REFERENCED ->
            setBackground(
                EditorTheme.rounded(
                    over ? EditorTheme.ROW_SELECTED : new Color(1f, 0.76f, 0.24f, 0.08f), null, 4));
        case NONE ->
            setBackground(over ? EditorTheme.rounded(EditorTheme.ROW_OVER, null, 4) : base);
      }
    }

    void base(Drawable drawable) {
      base = drawable;
      updateBackground();
    }

    boolean over() {
      return over;
    }
  }

  /**
   * The header of a group, with buttons to add a group inside of it, rename and remove it while it
   * is hovered.
   */
  private final class GroupRow extends Row {

    private final Group group;
    private final int depth;
    private final ImageButton addGroup;
    private final ImageButton rename;
    private final ImageButton remove;
    private boolean wasOver;

    private GroupRow(Group group, int depth, boolean open) {
      super(Highlight.NONE, null);
      this.group = group;
      this.depth = depth;
      if (!open && containsEntry(group.children(), selectedName)) base(hiddenSelection());
      padLeft(6f + depth * INDENT);
      add(disclosure(true, open)).width(DISCLOSURE).fillY();
      Image folder =
          new Image(EditorTheme.icon(open ? "folder-open" : "folder", EditorTheme.FOLDER));
      folder.setScaling(Scaling.fit);
      add(folder).size(ICON).padRight(7f);
      Label name = EditorTheme.label(group.name(), FONT, EditorTheme.TEXT);
      name.setEllipsis(true);
      add(name).growX().minWidth(0f);
      addGroup =
          EditorTheme.iconButton(
              EditorTheme.icon("folder-plus", EditorTheme.TEXT_MUTED),
              EditorTheme.Variant.GHOST,
              "New group inside");
      addGroup.getImageCell().size(13f);
      addGroup.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              event.stop();
              renameLater(listener.createGroup(group.name()));
            }
          });
      add(addGroup).size(22f);
      rename =
          EditorTheme.iconButton(
              EditorTheme.icon("pencil", EditorTheme.TEXT_MUTED),
              EditorTheme.Variant.GHOST,
              "Rename the group");
      rename.getImageCell().size(12f);
      rename.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              event.stop();
              startRename();
            }
          });
      add(rename).size(22f);
      remove =
          EditorTheme.iconButton(
              EditorTheme.icon("close", EditorTheme.TEXT_MUTED),
              EditorTheme.Variant.GHOST,
              "Remove the group, what is inside of it stays");
      remove.getImageCell().size(11f);
      remove.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              event.stop();
              listener.removeGroup(group.name());
            }
          });
      add(remove).size(22f);
      addGroup.setVisible(false);
      rename.setVisible(false);
      remove.setVisible(false);
      Label amount =
          EditorTheme.label(
              String.valueOf(entries(group.children()).size()), 12, EditorTheme.TEXT_DISABLED);
      amount.setAlignment(Align.right);
      add(amount).minWidth(20f).padLeft(4f).padRight(6f);
      addListener(
          new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              Actor target = event.getTarget();
              boolean button =
                  target != null
                      && (target.isDescendantOf(addGroup)
                          || target.isDescendantOf(rename)
                          || target.isDescendantOf(remove));
              if (!button) toggle(group.id());
            }
          });
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      addGroup.setVisible(over());
      rename.setVisible(over());
      remove.setVisible(over());
      if (over() != wasOver) {
        wasOver = over();
        listener.hoverGroup(
            wasOver ? entries(group.children()).stream().map(Entry::name).toList() : List.of());
      }
    }

    private void startRename() {
      clearChildren();
      padLeft(6f + depth * INDENT + DISCLOSURE);
      TextField field = EditorTheme.textField(group.name(), FONT);
      boolean[] done = {false};
      Consumer<String> finish =
          value -> {
            if (done[0]) return;
            done[0] = true;
            String name = value == null ? null : value.strip();
            if (name == null || name.equals(group.name())) {
              rebuild(false);
              return;
            }
            boolean wasExpanded = expanded.contains(group.id());
            if (listener.renameGroup(group.name(), name) && wasExpanded) {
              expanded.remove(group.id());
              expanded.add(groupId(name));
            }
          };
      field.setTextFieldListener(
          (textField, character) -> {
            if (character == '\r' || character == '\n') finish.accept(textField.getText());
          });
      field.addListener(
          new FocusListener() {
            @Override
            public void keyboardFocusChanged(
                FocusListener.FocusEvent event, Actor actor, boolean focused) {
              if (!focused) finish.accept(field.getText());
            }
          });
      field.addListener(
          new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
              if (keycode != Input.Keys.ESCAPE) return false;
              finish.accept(null);
              return true;
            }
          });
      add(field).growX().height(EditorTheme.ROW_HEIGHT - 2f).padRight(6f);
      // Focused once the row is shown, the list may still be built.
      addAction(
          Actions.run(
              () -> {
                if (getStage() == null) return;
                getStage().setKeyboardFocus(field);
                field.selectAll();
              }));
    }
  }
}
