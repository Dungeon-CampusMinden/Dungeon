package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.utils.Cursors;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasDragContext;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Items that players sort into bins on the canvas, by dragging a chip onto a bin or by clicking the
 * chip and then the bin.
 *
 * <p>Every item has a home slot (in a sentence, a word, a tray). A placed item's chip lies in its
 * bin and the slot shows a grey ghost of it; clicking that chip, or dragging it out of all bins,
 * puts the item back.
 *
 * <p>Bins handle their clicks themselves: the canvas raises every node it handles a press on, which
 * would cover the chips lying in them.
 */
final class SortBoard {

  /** Height of a chip. */
  static final float CHIP_HEIGHT = 36;

  /** Space between chips and between rows of chips. */
  static final float GAP = 8;

  private static final float BIN_GAP = 12;
  private static final float BIN_HEADER = 42;
  private static final float BIN_PAD = 12;
  private static final String HOME = "#home";
  private static final float TEXT_LEFT = 16;
  private static final float TEXT_RIGHT = 6;
  private static final float TEXT_BOTTOM = 4;

  private final CanvasArea area;
  private final Runnable onChange;
  private final Map<String, Chip> chips = new LinkedHashMap<>();
  private final Map<String, Slot> slots = new LinkedHashMap<>();
  private final Map<String, Bin> bins = new LinkedHashMap<>();
  private final Map<String, String> placement = new LinkedHashMap<>();
  private String selected;

  /**
   * Creates an empty board.
   *
   * @param area canvas that shows the chips, slots and bins
   * @param onChange called after every change of the placement
   */
  SortBoard(CanvasArea area, Runnable onChange) {
    this.area = area;
    this.onChange = onChange;
  }

  /**
   * Places chips in rows from left to right, wrapping at the given width.
   *
   * @param widths chip widths in order
   * @param maxWidth available width of a row
   * @return offset of every chip from the top left corner (y grows downwards) and the total height
   */
  static Flow flow(List<Float> widths, float maxWidth) {
    List<Vector2> offsets = new ArrayList<>();
    float x = 0;
    float y = 0;
    for (float width : widths) {
      if (x > 0 && x + width > maxWidth) {
        x = 0;
        y += CHIP_HEIGHT + GAP;
      }
      offsets.add(new Vector2(x, y));
      x += width + GAP;
    }
    return new Flow(offsets, widths.isEmpty() ? 0 : y + CHIP_HEIGHT);
  }

  /**
   * Lays out slots in rows on a belt or tray, right of the belt's own content such as a label.
   *
   * @param belt the belt; it gets the given width and the height its rows need
   * @param slots slots on the belt in order
   * @param top upper edge of the belt
   * @param width belt width
   * @return belt height
   */
  static float belt(PanelNode belt, List<Slot> slots, float top, float width) {
    float pad = 12;
    float indent = belt.content().getPrefWidth() + 24;
    Flow flow = flow(slots.stream().map(Slot::measure).toList(), width - indent - pad);
    float height = flow.height() + 2 * pad;
    belt.size(width, height).position(0, top - height);
    for (int index = 0; index < slots.size(); index++) {
      Vector2 offset = flow.offsets().get(index);
      slots.get(index).position(indent + offset.x, top - pad - offset.y - CHIP_HEIGHT);
    }
    return height;
  }

  /**
   * Lays out bins in a grid; the bins of a row share the height of the fullest one.
   *
   * @param bins bins in order
   * @param columns bins per row
   * @param left left edge of the grid
   * @param top upper edge of the grid
   * @param width grid width
   * @return grid height
   */
  static float grid(List<Bin> bins, int columns, float left, float top, float width) {
    float binWidth = (width - (columns - 1) * BIN_GAP) / columns;
    float y = top;
    for (int first = 0; first < bins.size(); first += columns) {
      List<Bin> row = bins.subList(first, Math.min(bins.size(), first + columns));
      float height = 0;
      for (Bin bin : row) height = Math.max(height, bin.fit(binWidth));
      for (int index = 0; index < row.size(); index++) {
        row.get(index)
            .size(binWidth, height)
            .position(left + index * (binWidth + BIN_GAP), y - height);
      }
      y -= height + BIN_GAP;
    }
    return top - y - BIN_GAP;
  }

  /**
   * Result of {@link #flow(List, float)}.
   *
   * @param offsets offset of every box from the top left corner, y grows downwards
   * @param height total height of all rows
   */
  record Flow(List<Vector2> offsets, float height) {}

  /**
   * Registers an item with its chip and home slot.
   *
   * @param id stable item ID
   * @param label visible label
   * @param size font size of the chip
   * @return home slot; the dialog positions it and the chip follows
   */
  Slot item(String id, String label, int size) {
    Chip chip = area.addNode(new Chip(id, label, size));
    chip.z(2);
    chips.put(id, chip);
    Slot slot = area.addNode(new Slot(id));
    slots.put(id, slot);
    return slot;
  }

  /**
   * Creates a bin.
   *
   * @param id bin ID
   * @param title localized bin title
   * @param capacity maximum number of items, {@code 0} for unlimited
   * @param minHeight minimum height of the bin
   * @return the bin
   */
  Bin bin(String id, String title, int capacity, float minHeight) {
    Bin bin = area.addNode(new Bin(id, title, capacity, minHeight));
    bins.put(id, bin);
    return bin;
  }

  /**
   * Removes all items whose ID starts with a prefix, including their placements.
   *
   * @param prefix ID prefix
   */
  void removeItems(String prefix) {
    placement.keySet().removeIf(id -> id.startsWith(prefix));
    for (String id : List.copyOf(chips.keySet())) {
      if (!id.startsWith(prefix)) continue;
      area.removeNode(chips.remove(id));
      area.removeNode(slots.remove(id));
    }
    if (selected != null && selected.startsWith(prefix)) selected = null;
  }

  /**
   * Places an item in a bin; a full bin returns its oldest item home.
   *
   * @param item item ID
   * @param bin bin ID
   */
  void place(String item, String bin) {
    if (!chips.containsKey(item) || !bins.containsKey(bin)) return;
    int capacity = bins.get(bin).capacity;
    if (capacity > 0 && !bin.equals(placement.get(item))) {
      List<String> inBin = itemsIn(bin);
      if (inBin.size() >= capacity) placement.remove(inBin.getFirst());
    }
    placement.remove(item);
    placement.put(item, bin);
    selected = null;
    onChange.run();
  }

  /**
   * Puts an item back home.
   *
   * @param item item ID
   */
  void remove(String item) {
    if (placement.remove(item) == null) return;
    onChange.run();
  }

  /** Puts all items back home. */
  void clear() {
    placement.clear();
    selected = null;
    onChange.run();
  }

  /**
   * Returns the current placement.
   *
   * @return copy of the bin of every placed item
   */
  Map<String, String> placement() {
    return new LinkedHashMap<>(placement);
  }

  /**
   * A click on the chip of an item.
   *
   * <p>At home it selects the item or drops the selection. In a bin it puts the item back, unless
   * another item is selected: then that one goes into this bin.
   *
   * @param item item ID
   */
  void clickChip(String item) {
    String bin = placement.get(item);
    if (bin == null) selected = item.equals(selected) ? null : item;
    else if (selected != null && !selected.equals(item)) place(selected, bin);
    else remove(item);
  }

  /**
   * A click on the grey ghost of a placed item selects it, so that the next bin click moves it.
   *
   * @param item item ID
   */
  void clickSlot(String item) {
    if (placement.containsKey(item)) selected = item.equals(selected) ? null : item;
  }

  /**
   * A click on a bin places the selected item there.
   *
   * @param bin bin ID
   */
  void clickBin(String bin) {
    if (selected != null) place(selected, bin);
  }

  /** Moves every chip to its slot or into its bin; slots and bins must be placed already. */
  void arrange() {
    for (Bin bin : bins.values()) {
      List<String> items = itemsIn(bin.id());
      Flow flow = flow(widths(items), bin.width() - 2 * BIN_PAD);
      float top = bin.y() + bin.height() - BIN_HEADER - BIN_PAD;
      for (int index = 0; index < items.size(); index++) {
        Vector2 offset = flow.offsets().get(index);
        chips
            .get(items.get(index))
            .position(bin.x() + BIN_PAD + offset.x, top - offset.y - CHIP_HEIGHT);
      }
    }
    chips.forEach(
        (id, chip) -> {
          chip.dragging = false;
          chip.setUserObject(Cursors.GRAB);
          Slot slot = slots.get(id);
          if (!placement.containsKey(id)) chip.position(slot.x(), slot.y());
        });
  }

  private List<String> itemsIn(String bin) {
    return placement.entrySet().stream()
        .filter(entry -> entry.getValue().equals(bin))
        .map(Map.Entry::getKey)
        .toList();
  }

  // A factory tag with its punched hole; a lifted tag casts a shadow, a lifted or chosen one has a
  // black edge.
  private static void drawTag(
      Batch batch, float alpha, CanvasNode tag, Color face, boolean lifted, boolean chosen) {
    float x = tag.x();
    float y = tag.y();
    if (lifted)
      CanvasGraphics.fill(
          batch, Color.BLACK, alpha * .35f, x + 5, y - 5, tag.width(), tag.height());
    MorphCoUI.plate(batch, face, alpha, x, y, tag.width(), tag.height());
    CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + 6, y + tag.height() / 2 - 3, 6, 6);
    if (lifted || chosen)
      CanvasGraphics.outline(batch, MorphCoUI.INK, alpha, x, y, tag.width(), tag.height(), 2);
  }

  private List<Float> widths(List<String> items) {
    return items.stream().map(id -> chips.get(id).measure()).toList();
  }

  /** The movable label of an item. */
  final class Chip extends CanvasNode {
    private final String text;
    private final int size;
    private Label label;
    private boolean dragging;
    private float startX;
    private float startY;

    private Chip(String id, String text, int size) {
      super(id, 60, CHIP_HEIGHT);
      this.text = text;
      this.size = size;
      deletable(false);
      selectable(false);
      setUserObject(Cursors.GRAB);
    }

    // Chips are as wide as their text, which needs the font.
    private float measure() {
      ensureContentBuilt();
      return width();
    }

    @Override
    protected void buildContent() {
      label = MorphCoUI.bold(text, size, MorphCoUI.INK);
      label.setWrap(false);
      label.setAlignment(Align.center);
      addActor(label);
      size(label.getPrefWidth() + 34, CHIP_HEIGHT);
    }

    @Override
    protected void layoutContent() {
      label.setBounds(
          TEXT_LEFT, TEXT_BOTTOM, width() - TEXT_LEFT - TEXT_RIGHT, height() - 2 * TEXT_BOTTOM);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      boolean chosen = id().equals(selected);
      drawTag(batch, alpha, this, chosen ? MorphCoUI.ORANGE : MorphCoUI.TAG, dragging, chosen);
    }

    @Override
    public void onMove(float dx, float dy) {
      if (!dragging) {
        dragging = true;
        startX = x();
        startY = y();
        setUserObject(Cursors.GRABBING);
      }
      super.onMove(dx, dy);
    }

    // A chip whose item stays where it was goes back to where the drag started.
    private void returnToStart() {
      dragging = false;
      setUserObject(Cursors.GRAB);
      position(startX, startY);
    }

    // Released outside of all bins, a placed item goes back home.
    @Override
    public void onDrop(float worldX, float worldY) {
      if (placement.containsKey(id())) SortBoard.this.remove(id());
      else returnToStart();
    }

    @Override
    public void onClick(float localX, float localY, int button) {
      if (button == 0) clickChip(id());
    }
  }

  /**
   * The home of an item: an empty recess, or a grey ghost while the item lies in a bin.
   *
   * <p>The ghost can be clicked or dragged like the chip: onto another bin to move the item there,
   * anywhere else to put it back.
   */
  final class Slot extends CanvasNode {
    private final String item;
    private Label ghost;
    private boolean dragging;
    private float homeX;
    private float homeY;

    private Slot(String item) {
      super(item + HOME, 60, CHIP_HEIGHT);
      this.item = item;
      deletable(false);
      selectable(false);
    }

    /**
     * Measures the item's chip; the slot has the same size.
     *
     * @return slot width
     */
    float measure() {
      ensureContentBuilt();
      return width();
    }

    @Override
    protected void buildContent() {
      Chip chip = chips.get(item);
      ghost = MorphCoUI.bold(chip.text, chip.size, Color.WHITE);
      ghost.setWrap(false);
      ghost.setAlignment(Align.center);
      addActor(ghost);
      size(chip.measure(), CHIP_HEIGHT);
    }

    @Override
    protected void layoutContent() {
      ghost.setBounds(
          TEXT_LEFT, TEXT_BOTTOM, width() - TEXT_LEFT - TEXT_RIGHT, height() - 2 * TEXT_BOTTOM);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      boolean placed = placement.containsKey(item);
      if (ghost != null) {
        ghost.setVisible(placed);
        ghost.setColor(dragging ? MorphCoUI.INK : MorphCoUI.GHOST);
      }
      setUserObject(placed ? dragging ? Cursors.GRABBING : Cursors.GRAB : Cursors.DEFAULT);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      if (dragging) {
        drawTag(batch, alpha, this, MorphCoUI.TAG, true, false);
        return;
      }
      CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x(), y(), width(), height());
      Color edge = item.equals(selected) ? MorphCoUI.ORANGE : MorphCoUI.GHOST;
      CanvasGraphics.outline(batch, edge, alpha * .8f, x(), y(), width(), height(), 1);
    }

    @Override
    public void onMove(float dx, float dy) {
      if (!placement.containsKey(item)) return;
      if (!dragging) {
        dragging = true;
        homeX = x();
        homeY = y();
      }
      super.onMove(dx, dy);
    }

    // The canvas raised the slot on the press; it goes back below the chips.
    private void returnHome() {
      z(0);
      if (!dragging) return;
      dragging = false;
      position(homeX, homeY);
    }

    @Override
    public void onDrop(float worldX, float worldY) {
      returnHome();
      SortBoard.this.remove(item);
    }

    @Override
    public void onClick(float localX, float localY, int button) {
      returnHome();
      if (button == 0) clickSlot(item);
    }
  }

  /** A crate, shaft or bowl that takes chips. */
  final class Bin extends CanvasNode {
    private final String title;
    private final int capacity;
    private final float minHeight;
    private Label heading;
    private boolean over;

    private Bin(String id, String title, int capacity, float minHeight) {
      super(id, 200, minHeight);
      this.title = title;
      this.capacity = capacity;
      this.minHeight = minHeight;
      movable(false);
      deletable(false);
      selectable(false);
      addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              clickBin(id());
            }
          });
    }

    /**
     * Sizes the bin for its chips at a given width.
     *
     * @param width bin width
     * @return bin height
     */
    float fit(float width) {
      Flow flow = flow(widths(itemsIn(id())), width - 2 * BIN_PAD);
      size(width, Math.max(minHeight, BIN_HEADER + 2 * BIN_PAD + flow.height()));
      return height();
    }

    @Override
    protected void buildContent() {
      heading = MorphCoUI.bold(title, 18, MorphCoUI.INK);
      heading.setWrap(false);
      heading.setEllipsis(true);
      addActor(heading);
    }

    @Override
    protected void layoutContent() {
      heading.setBounds(20, height() - BIN_HEADER + 6, width() - 40, BIN_HEADER - 14);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      setUserObject(selected == null ? Cursors.DEFAULT : Cursors.INTERACT);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x(), y(), width(), height());
      MorphCoUI.plate(
          batch,
          MorphCoUI.TAG,
          alpha,
          x() + 12,
          y() + height() - BIN_HEADER + 2,
          width() - 24,
          BIN_HEADER - 8);
      CanvasGraphics.fill(
          batch,
          over ? MorphCoUI.STEEL_DARK : MorphCoUI.INSET,
          alpha,
          x() + BIN_PAD - 4,
          y() + BIN_PAD - 4,
          width() - 2 * BIN_PAD + 8,
          height() - BIN_HEADER - 2 * BIN_PAD + 8);
      if (over || selected != null)
        CanvasGraphics.outline(
            batch,
            MorphCoUI.SIGNAL,
            alpha * (over ? 1 : .6f),
            x(),
            y(),
            width(),
            height(),
            over ? 4 : 2);
    }

    @Override
    public void onDragEnter(CanvasDragContext context) {
      onDragOver(context);
    }

    @Override
    public void onDragOver(CanvasDragContext context) {
      over = accepts(context);
    }

    @Override
    public void onDragExit(CanvasDragContext context) {
      over = false;
    }

    private boolean accepts(CanvasDragContext context) {
      return (context.draggedNode() instanceof Chip || context.draggedNode() instanceof Slot)
          && context.localX() >= 0
          && context.localX() < width()
          && context.localY() >= 0
          && context.localY() < height();
    }

    @Override
    public boolean onNodeDropped(CanvasDragContext context) {
      over = false;
      if (!accepts(context)) return false;
      CanvasNode dragged = context.draggedNode();
      String item = dragged.id();
      if (dragged instanceof Slot slot) {
        slot.returnHome();
        item = slot.item;
      }
      if (!id().equals(placement.get(item))) place(item, id());
      else if (dragged instanceof Chip chip) chip.returnToStart();
      return true;
    }
  }
}
