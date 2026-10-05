package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Ordered tree of prefab instances with collapsible, nestable groups and drag-and-drop reordering.
 *
 * <p>When reordering is enabled, items can be reordered among their siblings, i.e. top-level items
 * among each other and group members only within their group. The list is meant to be placed inside
 * a {@link ScrollPane}, which is scrolled automatically while dragging near its edges.
 */
public final class PrefabInstanceList extends Table {

  private static final int FONT_SIZE = 14;
  private static final float ROW_HEIGHT = 30f;
  private static final float GAP = 2f;
  private static final float SIDE_PAD = 6f;
  private static final float INDICATOR_THICKNESS = 2f;
  private static final float AUTO_SCROLL_MARGIN = 30f;
  private static final float AUTO_SCROLL_SPEED = 400f;
  private static final Color INDICATOR_COLOR = new Color(0.2f, 0.55f, 1f, 1f);

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
   * A single prefab instance.
   *
   * @param id unique item ID
   * @param name instance name
   * @param label displayed text
   */
  public record Entry(String id, String name, String label) implements Item {}

  /**
   * A group of items.
   *
   * @param id unique item ID
   * @param label displayed shared name part, also the initial rename value
   * @param children ordered child items
   */
  public record Group(String id, String label, List<Item> children) implements Item {
    /**
     * Creates a group with an immutable child list.
     *
     * @param id unique item ID
     * @param label displayed shared name part, also the initial rename value
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
     * Invoked when an entry is clicked.
     *
     * @param name clicked instance name
     */
    void select(String name);

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
  }

  private record DragData(String parentId, String itemId) {}

  private record Placement(int insertionIndex, float lineX, float lineY, float lineWidth) {}

  private final Listener listener;
  private final DragAndDrop dragAndDrop = new DragAndDrop();
  private final List<DragAndDrop.Source> sources = new ArrayList<>();
  private final Set<String> expandedGroups = new HashSet<>();
  private final List<Actor> rows = new ArrayList<>();
  private final Map<String, PrefabListGroup> groups = new HashMap<>();
  private final Image dropIndicator;
  private final Vector2 tmp = new Vector2();
  private List<Item> items = List.of();
  private String selectedName;
  private boolean reorderable;
  private Actor pendingScrollTarget;
  private float autoScrollDirection;

  /**
   * Creates an empty prefab instance list.
   *
   * @param listener receiver of user actions
   */
  public PrefabInstanceList(Listener listener) {
    this.listener = Objects.requireNonNull(listener, "listener");
    top().defaults().growX();
    dropIndicator =
        new Image(
            new TextureRegionDrawable(
                TextureMap.instance().textureAt(new SimpleIPath("hud/white.png"))));
    dropIndicator.setColor(INDICATOR_COLOR);
    dropIndicator.setTouchable(Touchable.disabled);
    dragAndDrop.setDragTime(0);
    dragAndDrop.addTarget(
        new DragAndDrop.Target(this) {
          @Override
          public boolean drag(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            updateAutoScroll(x, y);
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
            PrefabInstanceList.this.listener.moveItem(
                data.parentId(), data.itemId(), placement.insertionIndex());
          }
        });
  }

  /**
   * Rebuilds the list. When the selection changed, all groups containing the new selection are
   * expanded and the selected entry is scrolled into view.
   *
   * @param items top-level items in display order
   * @param selectedName selected instance name, or {@code null}
   * @param reorderable whether items can be reordered by drag and drop
   */
  public void setItems(List<Item> items, String selectedName, boolean reorderable) {
    boolean selectionChanged = !Objects.equals(selectedName, this.selectedName);
    boolean orderingChanged = reorderable != this.reorderable;
    this.items = List.copyOf(items);
    this.selectedName = selectedName;
    this.reorderable = reorderable;
    Set<String> groupIds = new HashSet<>();
    collectGroups(this.items, groupIds, selectionChanged);
    expandedGroups.retainAll(groupIds);
    rebuild(selectionChanged || orderingChanged);
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
    ScrollPane pane = scrollPane();
    if (pane == null) return;
    if (autoScrollDirection != 0f && dragAndDrop.isDragging()) {
      pane.setScrollY(pane.getScrollY() + autoScrollDirection * AUTO_SCROLL_SPEED * delta);
    }
    if (pendingScrollTarget != null && getStage() != null) {
      Actor target = pendingScrollTarget;
      pendingScrollTarget = null;
      pane.validate();
      if (target.isDescendantOf(this)) {
        Vector2 position = target.localToAscendantCoordinates(this, new Vector2());
        pane.scrollTo(position.x, position.y, target.getWidth(), target.getHeight());
      }
    }
  }

  private void rebuild(boolean scrollToSelection) {
    for (DragAndDrop.Source source : sources) dragAndDrop.removeSource(source);
    sources.clear();
    clearChildren();
    rows.clear();
    groups.clear();
    pendingScrollTarget = null;
    for (Item item : items) {
      Actor actor = build(item, null);
      rows.add(actor);
      Cell<Actor> cell = add(actor).growX().padTop(GAP).padLeft(SIDE_PAD).padRight(SIDE_PAD);
      if (item instanceof Entry) cell.height(ROW_HEIGHT);
      row();
    }
    if (!scrollToSelection) pendingScrollTarget = null;
  }

  private Actor build(Item item, String parentId) {
    return switch (item) {
      case Entry entry -> {
        TextButton button = entryButton(entry);
        addSource(button, new DragData(parentId, entry.id()), entry.label());
        if (entry.name().equals(selectedName)) pendingScrollTarget = button;
        yield button;
      }
      case Group group -> {
        boolean expanded = expandedGroups.contains(group.id());
        boolean containsSelection = group.contains(selectedName);
        String label = group.label();
        PrefabListGroup groupActor =
            new PrefabListGroup(
                label,
                group.entryCount(),
                expanded,
                containsSelection && !expanded,
                () -> toggle(group.id()),
                newLabel -> renameGroup(group.id(), label, newLabel));
        groups.put(group.id(), groupActor);
        addSource(
            groupActor.header(),
            new DragData(parentId, group.id()),
            label + " (" + group.entryCount() + ")");
        if (containsSelection && !expanded) pendingScrollTarget = groupActor.header();
        if (expanded) {
          for (Item child : group.children()) groupActor.addMember(build(child, group.id()));
        }
        yield groupActor;
      }
    };
  }

  private TextButton entryButton(Entry entry) {
    TextButton button =
        Scene2dElementFactory.createButton(
            entry.label(),
            Objects.equals(selectedName, entry.name()) ? "blue-outline" : "default",
            FONT_SIZE);
    button.getLabel().setAlignment(Align.left);
    button.getLabel().setEllipsis(true);
    button.getLabelCell().growX().minWidth(0f);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            listener.select(entry.name());
          }
        });
    return button;
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
    if (!reorderable) return;
    DragAndDrop.Source source =
        new DragAndDrop.Source(handle) {
          @Override
          public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
            DragAndDrop.Payload payload = new DragAndDrop.Payload();
            payload.setObject(data);
            TextButton ghost = Scene2dElementFactory.createButton(label, "blue-outline", FONT_SIZE);
            ghost.getLabel().setAlignment(Align.left);
            ghost.setSize(handle.getWidth(), handle.getHeight());
            ghost.getColor().a = 0.8f;
            ghost.setTouchable(Touchable.disabled);
            payload.setDragActor(ghost);
            dragAndDrop.setDragActorPosition(handle.getWidth() - x, -y);
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
      PrefabListGroup group = groups.get(data.parentId());
      if (group == null || !group.expanded()) return null;
      candidates = group.memberRows();
    }
    if (candidates.isEmpty()) return null;

    int insertionIndex = 0;
    for (Actor candidate : candidates) {
      Vector2 bottom = candidate.localToAscendantCoordinates(this, tmp.set(0f, 0f));
      if (bottom.y + candidate.getHeight() / 2f > y) insertionIndex++;
    }
    Actor reference = candidates.get(Math.min(insertionIndex, candidates.size() - 1));
    Vector2 bottomLeft = reference.localToAscendantCoordinates(this, tmp.set(0f, 0f));
    float lineY =
        insertionIndex < candidates.size()
            ? bottomLeft.y + reference.getHeight() + GAP / 2f
            : bottomLeft.y - GAP / 2f;
    return new Placement(insertionIndex, bottomLeft.x, lineY, reference.getWidth());
  }

  private void showIndicator(Placement placement) {
    if (dropIndicator.getParent() != this) addActor(dropIndicator);
    dropIndicator.toFront();
    dropIndicator.setBounds(
        placement.lineX(),
        placement.lineY() - INDICATOR_THICKNESS / 2f,
        placement.lineWidth(),
        INDICATOR_THICKNESS);
  }

  private void updateAutoScroll(float x, float y) {
    autoScrollDirection = 0f;
    ScrollPane pane = scrollPane();
    if (pane == null) return;
    Vector2 inPane = localToAscendantCoordinates(pane, tmp.set(x, y));
    if (inPane.y > pane.getHeight() - AUTO_SCROLL_MARGIN) autoScrollDirection = -1f;
    else if (inPane.y < AUTO_SCROLL_MARGIN) autoScrollDirection = 1f;
  }

  private ScrollPane scrollPane() {
    for (com.badlogic.gdx.scenes.scene2d.Group parent = getParent();
        parent != null;
        parent = parent.getParent()) {
      if (parent instanceof ScrollPane pane) return pane;
    }
    return null;
  }
}
