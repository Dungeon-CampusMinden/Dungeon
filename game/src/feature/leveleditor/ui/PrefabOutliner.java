package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
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
import java.util.function.Supplier;

/**
 * List of the prefab instances of a level, in the style of the outliners of common editors: a
 * search field, quick filters for instances that need attention, and a compact tree of collapsible
 * groups with an icon for every instance.
 *
 * <p>Clicking an instance selects it, double clicking also moves the camera to it. Hovering an
 * instance highlights it in the level. Groups are renamed with the pencil that shows when they are
 * hovered. In load order, items are reordered among their siblings by drag and drop.
 *
 * <p>While a search text or a filter is active, the matching instances are listed flat with their
 * full names and types.
 */
public final class PrefabOutliner extends Table {

  private static final float INDENT = 14f;
  private static final float ICON = 16f;
  private static final float DISCLOSURE = 14f;
  private static final float INDICATOR_THICKNESS = 2f;
  private static final float AUTO_SCROLL_MARGIN = 30f;
  private static final float AUTO_SCROLL_SPEED = 400f;
  private static final int FONT = 14;

  /** One list item. */
  public sealed interface Item permits Entry, Group {
    /**
     * Returns an identifier that is unique within the list.
     *
     * @return item ID
     */
    String id();
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
   * A single prefab instance.
   *
   * @param id unique item ID
   * @param name instance name
   * @param label displayed text inside its group
   * @param typeName display name of its prefab
   * @param icon creates the icon of its prefab
   * @param status state the list points out
   */
  public record Entry(
      String id, String name, String label, String typeName, Supplier<Actor> icon, Status status)
      implements Item {}

  /**
   * A group of items.
   *
   * @param id unique item ID
   * @param label displayed shared name part, also the initial rename value
   * @param area whether the group is an area, i.e. a name prefix before a slash
   * @param icon creates the icon of the group, or {@code null} for a folder
   * @param children ordered child items
   */
  public record Group(
      String id, String label, boolean area, Supplier<Actor> icon, List<Item> children)
      implements Item {
    /**
     * Creates a group with an immutable child list.
     *
     * @param id unique item ID
     * @param label displayed shared name part
     * @param area whether the group is an area
     * @param icon creates the icon of the group, or {@code null} for a folder
     * @param children ordered child items
     */
    public Group {
      children = List.copyOf(children);
    }

    private int entryCount() {
      int count = 0;
      for (Item child : children) {
        count += child instanceof Group group ? group.entryCount() : 1;
      }
      return count;
    }

    private boolean contains(String name) {
      if (name == null) return false;
      for (Item child : children) {
        if (child instanceof Entry entry && entry.name().equals(name)) return true;
        if (child instanceof Group group && group.contains(name)) return true;
      }
      return false;
    }
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
     * Invoked when a group's shared name part should be renamed.
     *
     * @param groupId ID of the group
     * @param label current label
     * @param newLabel requested label
     * @return ID of the renamed group, or {@code null} if the rename was rejected
     */
    String renameGroup(String groupId, String label, String newLabel);

    /**
     * Invoked when an item was dropped at a new position among its siblings.
     *
     * @param parentId ID of the parent group, or {@code null} for top-level items
     * @param itemId ID of the moved item
     * @param insertionIndex index of the sibling it is inserted in front of, or the sibling count
     */
    void moveItem(String parentId, String itemId, int insertionIndex);

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

  private record DragData(String parentId, String itemId) {}

  private record Placement(int insertionIndex, float lineX, float lineY, float lineWidth) {}

  private final Listener listener;
  private final DragAndDrop dragAndDrop = new DragAndDrop();
  private final List<DragAndDrop.Source> sources = new ArrayList<>();
  private final Set<String> expandedGroups = new HashSet<>();
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
  private final List<Actor> rows = new ArrayList<>();
  private final Map<String, GroupBlock> groups = new HashMap<>();
  private final Image dropIndicator;
  private final Vector2 tmp = new Vector2();
  private List<Item> items = List.of();
  private String selectedName;
  private boolean loadOrder;
  private Actor pendingScrollTarget;
  private float autoScrollDirection;
  private String hovered;
  // Instance clicked in this list, its selection does not scroll the list.
  private String clickedName;

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
    ImageButton expand =
        EditorTheme.iconButton(
            EditorTheme.icon("expand-all"),
            EditorTheme.Variant.GHOST,
            "Expand all groups by one level");
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
            EditorTheme.icon("collapse-all"), EditorTheme.Variant.GHOST, "Collapse all groups");
    collapse.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            expandedGroups.clear();
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
    search.setMessageText("Search name or type  (Ctrl+F)");
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
            Placement placement = placement((DragData) payload.getObject(), y);
            if (placement == null) {
              dropIndicator.remove();
              return false;
            }
            showIndicator(placement);
            return true;
          }

          @Override
          public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
            dropIndicator.remove();
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
            Placement placement = placement(data, y);
            if (placement == null) return;
            PrefabOutliner.this.listener.moveItem(
                data.parentId(), data.itemId(), placement.insertionIndex());
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
   * Rebuilds the list. When the selection changed, all groups containing the new selection are
   * expanded and the selected entry is scrolled into view.
   *
   * @param items top-level items in display order
   * @param selectedName selected instance name, or {@code null}
   * @param loadOrder whether the items are in load order, which allows reordering them
   */
  public void setItems(List<Item> items, String selectedName, boolean loadOrder) {
    boolean selectionChanged = !Objects.equals(selectedName, this.selectedName);
    // A row clicked in this list is in view already, so the list stays where it is.
    boolean selectedHere = selectionChanged && Objects.equals(selectedName, clickedName);
    if (selectionChanged) clickedName = null;
    boolean orderChanged = loadOrder != this.loadOrder;
    this.items = List.copyOf(items);
    this.selectedName = selectedName;
    this.loadOrder = loadOrder;
    Set<String> groupIds = new HashSet<>();
    collectGroups(this.items, groupIds, selectionChanged);
    expandedGroups.retainAll(groupIds);
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
    rebuild((selectionChanged && !selectedHere) || orderChanged);
  }

  private void collectGroups(List<Item> items, Set<String> groupIds, boolean expandSelection) {
    for (Item item : items) {
      if (!(item instanceof Group group)) continue;
      groupIds.add(group.id());
      if (expandSelection && group.contains(selectedName)) expandedGroups.add(group.id());
      collectGroups(group.children(), groupIds, expandSelection);
    }
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
    rows.clear();
    groups.clear();
    pendingScrollTarget = null;
    clearSearch.setVisible(!search.getText().isEmpty());
    // The rows under the cursor are replaced, they report hovering again when the cursor moves.
    if (hovered != null) {
      hovered = null;
      listener.hover(null);
    }
    listener.hoverGroup(List.of());

    List<Entry> all = new ArrayList<>();
    collectEntries(items, all);
    count.setText(String.valueOf(all.size()));
    rebuildFilters(all);

    String query = search.getText().strip().toLowerCase(Locale.ROOT);
    if (!query.isEmpty() || !filters.isEmpty()) {
      List<Entry> matches = new ArrayList<>();
      for (Entry entry : all) {
        if (matches(entry, query)) matches.add(entry);
      }
      for (Entry entry : matches) {
        Actor row = entryRow(entry, 0, entry.name(), true);
        list.add(row).height(EditorTheme.ROW_HEIGHT).row();
        if (entry.name().equals(selectedName)) pendingScrollTarget = row;
      }
      if (matches.isEmpty()) {
        Label empty = EditorTheme.label("No prefab matches.", 14, EditorTheme.TEXT_MUTED);
        list.add(empty).pad(10f, 8f, 0f, 8f).left().row();
      }
    } else {
      for (Item item : items) {
        Actor actor = build(item, null, 0);
        rows.add(actor);
        var cell = list.add(actor);
        if (item instanceof Entry) cell.height(EditorTheme.ROW_HEIGHT);
        list.row();
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
  }

  private static void collectNames(List<Item> items, List<String> result) {
    for (Item item : items) {
      switch (item) {
        case Entry entry -> result.add(entry.name());
        case Group group -> collectNames(group.children(), result);
      }
    }
  }

  private static void collectEntries(List<Item> items, List<Entry> result) {
    for (Item item : items) {
      switch (item) {
        case Entry entry -> result.add(entry);
        case Group group -> collectEntries(group.children(), result);
      }
    }
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
    String text = (entry.name() + " " + entry.typeName()).toLowerCase(Locale.ROOT);
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

  private Actor build(Item item, String parentId, int depth) {
    return switch (item) {
      case Entry entry -> {
        Actor row = entryRow(entry, depth, entry.label(), false);
        addSource(row, new DragData(parentId, entry.id()), entry.label());
        if (entry.name().equals(selectedName)) pendingScrollTarget = row;
        yield row;
      }
      case Group group -> {
        GroupBlock block = new GroupBlock(group, depth);
        groups.put(group.id(), block);
        addSource(block.header, new DragData(parentId, group.id()), group.label());
        if (block.highlighted) pendingScrollTarget = block.header;
        if (block.expanded) {
          for (Item child : group.children()) block.addMember(build(child, group.id(), depth + 1));
        }
        yield block;
      }
    };
  }

  private Table entryRow(Entry entry, int depth, String label, boolean flat) {
    boolean selected = entry.name().equals(selectedName);
    Row row = new Row(selected, entry.name());
    row.padLeft(6f + depth * INDENT + (flat ? 0f : DISCLOSURE));
    row.add(entry.icon().get()).size(ICON).padRight(7f);
    Color color =
        selected
            ? EditorTheme.SELECTION
            : entry.status().inactive() ? EditorTheme.TEXT_DISABLED : EditorTheme.TEXT;
    Label name = EditorTheme.label(label, FONT, color);
    name.setEllipsis(true);
    row.add(name).growX().minWidth(0f);
    if (flat) {
      Label type = EditorTheme.label(entry.typeName(), 12, EditorTheme.TEXT_DISABLED);
      row.add(type).padLeft(6f);
    }
    addStatus(row, entry.status());
    row.padRight(6f);
    row.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (getTapCount() >= 2) {
              listener.frame(entry.name());
            } else {
              clickedName = entry.name();
              listener.select(entry.name());
            }
          }
        });
    return row;
  }

  private static void addStatus(Table row, Status status) {
    if (status.detached()) {
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

  /**
   * Expands the groups of the topmost level that is not fully expanded yet: the first click expands
   * all top-level groups, the next one all groups inside of them, and so on.
   */
  private void expandLevel() {
    List<List<Group>> levels = new ArrayList<>();
    collectLevels(items, 0, levels);
    for (List<Group> level : levels) {
      boolean expanded = true;
      for (Group group : level) expanded &= expandedGroups.contains(group.id());
      if (expanded) continue;
      for (Group group : level) expandedGroups.add(group.id());
      break;
    }
    rebuild(false);
  }

  private static void collectLevels(List<Item> items, int depth, List<List<Group>> levels) {
    for (Item item : items) {
      if (!(item instanceof Group group)) continue;
      while (levels.size() <= depth) levels.add(new ArrayList<>());
      levels.get(depth).add(group);
      collectLevels(group.children(), depth + 1, levels);
    }
  }

  private void toggle(String groupId) {
    if (!expandedGroups.remove(groupId)) expandedGroups.add(groupId);
    rebuild(false);
  }

  private void renameGroup(String groupId, String label, String newLabel) {
    boolean expanded = expandedGroups.contains(groupId);
    String renamedId = listener.renameGroup(groupId, label, newLabel);
    if (renamedId != null && expanded) {
      expandedGroups.remove(groupId);
      expandedGroups.add(renamedId);
    }
  }

  private void addSource(Actor handle, DragData data, String label) {
    if (!loadOrder) return;
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

  private Placement placement(DragData data, float y) {
    List<Actor> candidates;
    if (data.parentId() == null) {
      candidates = rows;
    } else {
      GroupBlock group = groups.get(data.parentId());
      if (group == null || !group.expanded) return null;
      candidates = group.memberRows;
    }
    if (candidates.isEmpty()) return null;

    int insertionIndex = 0;
    for (Actor candidate : candidates) {
      Vector2 bottom = candidate.localToAscendantCoordinates(list, tmp.set(0f, 0f));
      if (bottom.y + candidate.getHeight() / 2f > y) insertionIndex++;
    }
    Actor reference = candidates.get(Math.min(insertionIndex, candidates.size() - 1));
    Vector2 bottomLeft = reference.localToAscendantCoordinates(list, tmp.set(0f, 0f));
    float lineY =
        insertionIndex < candidates.size() ? bottomLeft.y + reference.getHeight() : bottomLeft.y;
    return new Placement(insertionIndex, bottomLeft.x, lineY, reference.getWidth());
  }

  private void showIndicator(Placement placement) {
    if (dropIndicator.getParent() != list) list.addActor(dropIndicator);
    dropIndicator.toFront();
    dropIndicator.setBounds(
        placement.lineX(),
        placement.lineY() - INDICATOR_THICKNESS / 2f,
        placement.lineWidth(),
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

  /** A clickable row with hover and selection backgrounds. */
  private class Row extends Table {

    private final boolean selected;
    private Drawable base;
    private boolean over;

    private Row(boolean selected, String hoverName) {
      this.selected = selected;
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
      if (selected) {
        setBackground(EditorTheme.barred(EditorTheme.ROW_SELECTED, EditorTheme.SELECTION));
      } else if (over) {
        setBackground(EditorTheme.rounded(EditorTheme.ROW_OVER, null, 4));
      } else {
        setBackground(base);
      }
    }

    private void base(Drawable drawable) {
      base = drawable;
      updateBackground();
    }

    boolean over() {
      return over;
    }
  }

  /** A group header with its members below it while expanded. */
  private final class GroupBlock extends Table {

    private final Group group;
    private final boolean expanded;
    private final boolean highlighted;
    private final Table header;
    private final Table members = new Table();
    private final List<Actor> memberRows = new ArrayList<>();
    private final int depth;
    private ImageButton rename;

    private GroupBlock(Group group, int depth) {
      this.group = group;
      this.depth = depth;
      this.expanded = expandedGroups.contains(group.id());
      this.highlighted = !expanded && group.contains(selectedName);
      top().defaults().growX().minWidth(0f);
      members.top().defaults().growX().minWidth(0f);
      header = headerRow();
      add(header).height(EditorTheme.ROW_HEIGHT).row();
      if (expanded) add(members).row();
    }

    private Table headerRow() {
      Row row =
          new Row(false, null) {
            private boolean wasOver;

            @Override
            public void act(float delta) {
              super.act(delta);
              if (rename != null) rename.setVisible(over());
              if (over() != wasOver) {
                wasOver = over();
                List<String> names = new ArrayList<>();
                if (wasOver) collectNames(group.children(), names);
                listener.hoverGroup(names);
              }
            }
          };
      if (highlighted) {
        row.base(EditorTheme.barred(new Color(1f, 0.76f, 0.24f, 0.07f), EditorTheme.SELECTION));
      }
      row.padLeft(6f + depth * INDENT);
      Image chevron =
          new Image(
              EditorTheme.icon(
                  expanded ? "chevron-down" : "chevron-right", EditorTheme.TEXT_MUTED));
      chevron.setScaling(Scaling.fit);
      row.add(chevron).size(9f).padRight(5f);
      Actor icon;
      if (group.icon() != null) {
        icon = group.icon().get();
      } else {
        Image folder =
            new Image(
                EditorTheme.icon(
                    expanded ? "folder-open" : "folder", new Color(0.85f, 0.72f, 0.42f, 1f)));
        folder.setScaling(Scaling.fit);
        icon = folder;
      }
      row.add(icon).size(ICON).padRight(7f);
      Label name = EditorTheme.label(group.label(), FONT, EditorTheme.TEXT);
      name.setEllipsis(true);
      row.add(name).growX().minWidth(0f);
      rename =
          EditorTheme.iconButton(
              EditorTheme.icon("pencil", EditorTheme.TEXT_MUTED),
              EditorTheme.Variant.GHOST,
              group.area()
                  ? "Rename the area of all its prefabs"
                  : "Rename all prefabs of the group");
      rename.getImageCell().size(12f);
      rename.setVisible(false);
      rename.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              event.stop();
              startRename();
            }
          });
      row.add(rename).size(22f);
      Label amount =
          EditorTheme.label(String.valueOf(group.entryCount()), 12, EditorTheme.TEXT_DISABLED);
      amount.setAlignment(Align.right);
      row.add(amount).minWidth(20f).padLeft(4f).padRight(6f);
      row.addListener(
          new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (event.getTarget() != null && event.getTarget().isDescendantOf(rename)) return;
              toggle(group.id());
            }
          });
      return row;
    }

    private void addMember(Actor member) {
      var cell = members.add(member);
      if (!(member instanceof GroupBlock)) cell.height(EditorTheme.ROW_HEIGHT);
      members.row();
      memberRows.add(member);
    }

    private void startRename() {
      header.clearChildren();
      header.padLeft(6f + depth * INDENT + DISCLOSURE);
      TextField field = EditorTheme.textField(group.label(), FONT);
      boolean[] done = {false};
      Consumer<String> finish =
          value -> {
            if (done[0]) return;
            done[0] = true;
            if (value == null || value.equals(group.label())) rebuild(false);
            else renameGroup(group.id(), group.label(), value);
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
      header.add(field).growX().height(EditorTheme.ROW_HEIGHT - 2f).padRight(6f);
      if (getStage() != null) {
        getStage().setKeyboardFocus(field);
        field.selectAll();
      }
    }
  }
}
