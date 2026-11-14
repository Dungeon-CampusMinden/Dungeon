package feature.interaction.sorting;

import static feature.interaction.FeatureDialogStyle.INK;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.utils.Cursors;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
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
 *
 * <p>The dialog places slots and bins, e.g. with {@link #flow} and {@link #grid}, and then calls
 * {@link #arrange()}. The board draws in the plain look of the engine's {@link SortingUI}; a room
 * gives it its own look by overriding {@link #label}, {@link #ink}, {@link #ghost}, {@link
 * #drawChip}, {@link #drawSlot} and {@link #drawBin}.
 */
public class SortBoard {

  /** Height of a chip. */
  public static final float CHIP_HEIGHT = 36;

  /** Space between chips and between rows of chips. */
  static final float GAP = 8;

  /** Height of the title strip at the top of a bin. */
  protected static final float BIN_HEADER = 42;

  /** Space between the edge of a bin and its chips. */
  protected static final float BIN_PAD = 12;

  private static final float BIN_GAP = 12;
  private static final String HOME = "#home";
  private static final float TEXT_LEFT = 16;
  private static final float TEXT_RIGHT = 6;
  private static final float TEXT_BOTTOM = 4;

  private static final Color GHOST = Color.valueOf("8a919e");
  private static final Color CARD = Color.valueOf("fbfbf8");
  private static final Color CHOSEN = Color.valueOf("f6d76b");
  private static final Color EDGE = Color.valueOf("5b6474");
  private static final Color RECESS = Color.valueOf("e2ddd0");
  private static final Color BIN = Color.valueOf("d3ccbc");
  private static final Color WELL = Color.valueOf("f7f5ef");
  private static final Color WELL_OVER = Color.valueOf("dce8f5");
  private static final Color SIGNAL = Color.valueOf("3b6ea8");

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
  public SortBoard(CanvasArea area, Runnable onChange) {
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
  public static Flow flow(List<Float> widths, float maxWidth) {
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
   * Lays out bins in a grid; the bins of a row share the height of the fullest one.
   *
   * @param bins bins in order
   * @param columns bins per row
   * @param left left edge of the grid
   * @param top upper edge of the grid
   * @param width grid width
   * @return grid height
   */
  public static float grid(List<Bin> bins, int columns, float left, float top, float width) {
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
  public record Flow(List<Vector2> offsets, float height) {}

  /**
   * Registers an item with its chip and home slot.
   *
   * @param id stable item ID
   * @param label visible label
   * @param size font size of the chip
   * @return home slot; the dialog positions it and the chip follows
   */
  public Slot item(String id, String label, int size) {
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
  public Bin bin(String id, String title, int capacity, float minHeight) {
    Bin bin = area.addNode(new Bin(id, title, capacity, minHeight));
    bins.put(id, bin);
    return bin;
  }

  /**
   * Removes all items whose ID starts with a prefix, including their placements.
   *
   * @param prefix ID prefix
   */
  public void removeItems(String prefix) {
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
  public void place(String item, String bin) {
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
  public void clear() {
    placement.clear();
    selected = null;
    onChange.run();
  }

  /**
   * Returns the current placement.
   *
   * @return copy of the bin of every placed item
   */
  public Map<String, String> placement() {
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
  public void arrange() {
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

  private List<Float> widths(List<String> items) {
    return items.stream().map(id -> chips.get(id).measure()).toList();
  }

  // The look creates the label; wrapping, alignment and clicks stay with the board.
  private Label text(String text, int size, Color color, int align) {
    Label label = label(text, size, color);
    label.setWrap(false);
    label.setAlignment(align);
    label.setTouchable(Touchable.disabled);
    return label;
  }

  /**
   * Creates the text of a chip, of a ghost or of a bin title.
   *
   * @param text displayed text
   * @param size logical font size
   * @param color text color
   * @return the label
   */
  protected Label label(String text, int size, Color color) {
    return Scene2dElementFactory.createLabel(
        text, FontSpec.of(Scene2dElementFactory.FONT_PATH_BOLD, size, color));
  }

  /**
   * Returns the color of chip texts and bin titles.
   *
   * @return text color
   */
  protected Color ink() {
    return INK;
  }

  /**
   * Returns the color of the grey ghost that a placed item leaves in its slot.
   *
   * @return ghost color
   */
  protected Color ghost() {
    return GHOST;
  }

  /**
   * Draws a chip, or the ghost of a placed item while it is dragged. The text is drawn on top, with
   * some more room on the left than on the right.
   *
   * @param batch batch to draw with
   * @param alpha inherited alpha
   * @param chip the chip or slot
   * @param lifted whether it is being dragged
   * @param chosen whether its item is selected, so a click on a bin places it there
   */
  protected void drawChip(
      Batch batch, float alpha, CanvasNode chip, boolean lifted, boolean chosen) {
    float x = chip.x();
    float y = chip.y();
    if (lifted)
      CanvasGraphics.fill(
          batch, Color.BLACK, alpha * .25f, x + 4, y - 4, chip.width(), chip.height());
    CanvasGraphics.fill(batch, chosen ? CHOSEN : CARD, alpha, x, y, chip.width(), chip.height());
    // A grip on the left shows that the chip can be moved.
    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 2; column++) {
        CanvasGraphics.fill(batch, GHOST, alpha, x + 5 + 4 * column, y + 12 + 5 * row, 2, 2);
      }
    }
    CanvasGraphics.outline(
        batch, lifted || chosen ? INK : EDGE, alpha, x, y, chip.width(), chip.height(), 2);
  }

  /**
   * Draws the empty home slot of an item; the ghost of a placed item is drawn on top.
   *
   * @param batch batch to draw with
   * @param alpha inherited alpha
   * @param slot the slot
   * @param chosen whether its item is selected, so a click on a bin moves it there
   */
  protected void drawSlot(Batch batch, float alpha, CanvasNode slot, boolean chosen) {
    CanvasGraphics.fill(batch, RECESS, alpha, slot.x(), slot.y(), slot.width(), slot.height());
    CanvasGraphics.outline(
        batch,
        chosen ? SIGNAL : GHOST,
        alpha,
        slot.x(),
        slot.y(),
        slot.width(),
        slot.height(),
        chosen ? 2 : 1);
  }

  /**
   * Draws a bin with a title strip of {@link #BIN_HEADER} at the top, whose title is drawn on top,
   * and room for chips {@link #BIN_PAD} inside its edges.
   *
   * @param batch batch to draw with
   * @param alpha inherited alpha
   * @param bin the bin
   * @param over whether a chip is held over the bin
   * @param armed whether an item is selected, so a click on the bin places it there
   */
  protected void drawBin(Batch batch, float alpha, CanvasNode bin, boolean over, boolean armed) {
    CanvasGraphics.fill(batch, BIN, alpha, bin.x(), bin.y(), bin.width(), bin.height());
    CanvasGraphics.fill(
        batch,
        over ? WELL_OVER : WELL,
        alpha,
        bin.x() + BIN_PAD - 4,
        bin.y() + BIN_PAD - 4,
        bin.width() - 2 * BIN_PAD + 8,
        bin.height() - BIN_HEADER - 2 * BIN_PAD + 8);
    CanvasGraphics.outline(
        batch,
        over || armed ? SIGNAL : EDGE,
        alpha,
        bin.x(),
        bin.y(),
        bin.width(),
        bin.height(),
        over ? 4 : 2);
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
      label = text(text, size, ink(), Align.center);
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
      drawChip(batch, alpha, this, dragging, id().equals(selected));
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
  public final class Slot extends CanvasNode {
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
    public float measure() {
      ensureContentBuilt();
      return width();
    }

    @Override
    protected void buildContent() {
      Chip chip = chips.get(item);
      ghost = text(chip.text, chip.size, Color.WHITE, Align.center);
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
        ghost.setColor(dragging ? ink() : ghost());
      }
      setUserObject(placed ? dragging ? Cursors.GRABBING : Cursors.GRAB : Cursors.DEFAULT);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      if (dragging) drawChip(batch, alpha, this, true, false);
      else drawSlot(batch, alpha, this, item.equals(selected));
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

  /** A crate, shaft, bowl or box that takes chips. */
  public final class Bin extends CanvasNode {
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
    public float fit(float width) {
      Flow flow = flow(widths(itemsIn(id())), width - 2 * BIN_PAD);
      size(width, Math.max(minHeight, BIN_HEADER + 2 * BIN_PAD + flow.height()));
      return height();
    }

    @Override
    protected void buildContent() {
      heading = text(title, 18, ink(), Align.topLeft);
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
      drawBin(batch, alpha, this, over, selected != null);
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
