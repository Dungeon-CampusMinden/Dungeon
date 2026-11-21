package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import engine.input.GameInput;
import engine.input.InputDevice;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.PadNavigation;
import feature.interaction.sorting.SortBoard;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The engine's sort board in the look of the hall: chips are factory tags with a punched hole, bins
 * are the shafts, compartments, crates or bowls of the machines, each with a name plate. A chip
 * that lands in a bin drops into it from above.
 */
final class MorphCoSortBoard extends SortBoard {

  /** What the bins of a board are. */
  enum Look {
    /** Input shafts of a machine: a hopper behind a mouth with warning stripes. */
    SHAFT,
    /** Compartments inside a machine: a ribbed tray behind a mouth with warning stripes. */
    COMPARTMENT,
    /** Open steel crates seen from above, like the crates in the hall. */
    CRATE,
    /** Steel bowls on a workbench; a bowl holds one chip in its hollow. */
    BOWL
  }

  private static final float DROP = 48;
  private static final float DROP_SECONDS = .3f;

  private final CanvasArea area;
  private final Look look;
  // Items placed since the last arrange() drop into their bin; their chips fall until done.
  private final Set<String> dropped = new HashSet<>();
  private final Set<CanvasNode> falling = new HashSet<>();

  MorphCoSortBoard(CanvasArea area, Look look, Runnable onChange) {
    super(area, onChange);
    this.area = area;
    this.look = look;
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
    // An emptied tray keeps the height of one row for its label.
    float height = Math.max(flow.height(), CHIP_HEIGHT) + 2 * pad;
    belt.size(width, height).position(0, top - height);
    for (int index = 0; index < slots.size(); index++) {
      Vector2 offset = flow.offsets().get(index);
      slots.get(index).position(indent + offset.x, top - pad - offset.y - CHIP_HEIGHT);
    }
    return height;
  }

  @Override
  public void place(String item, String bin) {
    dropped.add(item);
    super.place(item, bin);
  }

  /**
   * Puts an item back into a bin without the fall, e.g. when a dialog opens with earlier work.
   *
   * @param item item ID
   * @param bin bin ID
   */
  void restore(String item, String bin) {
    super.place(item, bin);
  }

  @Override
  public void arrange() {
    // A chip that is sent home or moves on while it falls stops falling.
    falling.forEach(CanvasNode::clearActions);
    falling.clear();
    super.arrange();
    placement()
        .forEach((item, bin) -> {
          CanvasNode chip = area.nodeById(item).orElseThrow();
          if (look == Look.BOWL) {
            CanvasNode bowl = area.nodeById(bin).orElseThrow();
            Vector2 hollow =
                MorphCoUI.hollow(bowl.x(), bowl.y(), bowl.width(), bowl.height() - BIN_HEADER);
            chip.position(hollow.x - chip.width() / 2, hollow.y - chip.height() / 2);
            // The pad cursor goes into the bowl with the chip, so the D-pad moves on from
            // there.
            if (dropped.contains(item) && GameInput.device() == InputDevice.PAD)
              PadNavigation.point(chip);
          }
          if (!dropped.contains(item)) return;
          float y = chip.y();
          chip.setY(y + DROP);
          chip.addAction(Actions.moveTo(chip.x(), y, DROP_SECONDS, Interpolation.bounceOut));
          falling.add(chip);
        });
    dropped.clear();
  }

  @Override
  protected Label label(String text, int size, Color color) {
    return MorphCoUI.bold(text, size, color);
  }

  @Override
  protected Color ink() {
    return MorphCoUI.INK;
  }

  @Override
  protected Color ghost() {
    return MorphCoUI.GHOST;
  }

  // A factory tag with its punched hole; a lifted tag casts a shadow, a lifted or chosen one has a
  // black edge.
  @Override
  protected void drawChip(
      Batch batch, float alpha, CanvasNode tag, boolean lifted, boolean chosen) {
    float x = tag.x();
    float y = tag.y();
    if (lifted)
      CanvasGraphics.fill(
          batch, Color.BLACK, alpha * .35f, x + 5, y - 5, tag.width(), tag.height());
    Color face = chosen ? MorphCoUI.ORANGE : MorphCoUI.TAG;
    MorphCoUI.plate(batch, face, alpha, x, y, tag.width(), tag.height());
    CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x + 6, y + tag.height() / 2 - 3, 6, 6);
    if (lifted || chosen)
      CanvasGraphics.outline(batch, MorphCoUI.INK, alpha, x, y, tag.width(), tag.height(), 2);
  }

  @Override
  protected void drawSlot(Batch batch, float alpha, CanvasNode slot, boolean chosen) {
    float x = slot.x();
    float y = slot.y();
    CanvasGraphics.fill(batch, MorphCoUI.INSET, alpha, x, y, slot.width(), slot.height());
    Color edge = chosen ? MorphCoUI.ORANGE : MorphCoUI.GHOST;
    CanvasGraphics.outline(batch, edge, alpha * .8f, x, y, slot.width(), slot.height(), 1);
  }

  @Override
  protected void drawBin(Batch batch, float alpha, CanvasNode bin, boolean over, boolean armed) {
    float x = bin.x();
    float y = bin.y();
    float width = bin.width();
    float height = bin.height();
    if (look == Look.BOWL) {
      Color edge = over || armed ? MorphCoUI.SIGNAL : null;
      MorphCoUI.bowl(batch, alpha, x, y, width, height - BIN_HEADER, edge);
    } else if (look == Look.CRATE) {
      drawCrate(batch, alpha, x, y, width, height, over);
    } else {
      drawMachine(batch, alpha, x, y, width, height, over);
    }
    plaque(batch, alpha, x, y, width, height);
    if (look != Look.BOWL && (over || armed))
      CanvasGraphics.outline(
          batch, MorphCoUI.SIGNAL, alpha * (over ? 1 : .6f), x, y, width, height, over ? 4 : 2);
  }

  /**
   * Draws the name plate in the top strip of a bin; its title is drawn on top.
   *
   * @param batch batch to draw with
   * @param alpha inherited alpha
   * @param x left edge of the bin
   * @param y bottom edge of the bin
   * @param width bin width
   * @param height bin height
   */
  static void plaque(Batch batch, float alpha, float x, float y, float width, float height) {
    MorphCoUI.plate(
        batch,
        MorphCoUI.TAG,
        alpha,
        x + 12,
        y + height - BIN_HEADER + 2,
        width - 24,
        BIN_HEADER - 8);
  }

  // Housing of a machine with the dark mouth of a shaft or compartment below the name plate.
  private void drawMachine(
      Batch batch, float alpha, float x, float y, float width, float height, boolean over) {
    MorphCoUI.plate(batch, MorphCoUI.STEEL_DARK, alpha, x, y, width, height);
    float innerX = x + BIN_PAD - 4;
    float innerY = y + BIN_PAD - 4;
    float innerWidth = width - 2 * BIN_PAD + 8;
    float top = y + height - BIN_HEADER - BIN_PAD + 4;
    CanvasGraphics.fill(batch, MorphCoUI.OUTLINE, alpha, innerX, innerY, innerWidth, top - innerY);
    if (look == Look.SHAFT) {
      // The hopper narrows below the first row of chips into an outlet through the housing.
      float funnel = (top - innerY) * .55f;
      float outlet = Math.min(96, innerWidth * .3f);
      float rails = top - innerY - funnel;
      CanvasGraphics.fill(batch, MorphCoUI.GROOVE, alpha, innerX + 6, innerY + funnel, 2, rails);
      CanvasGraphics.fill(
          batch, MorphCoUI.GROOVE, alpha, innerX + innerWidth - 8, innerY + funnel, 2, rails);
      MorphCoUI.funnel(batch, alpha, innerX, innerY, innerWidth, funnel, outlet);
      CanvasGraphics.fill(
          batch, MorphCoUI.OUTLINE, alpha, x + (width - outlet) / 2, y, outlet, BIN_PAD - 4);
    } else {
      for (float rib = innerY + 8; rib < top - 12; rib += 10)
        CanvasGraphics.fill(batch, MorphCoUI.GROOVE, alpha, innerX + 4, rib, innerWidth - 8, 1);
      CanvasGraphics.fill(batch, MorphCoUI.STEEL, alpha, innerX, innerY, innerWidth, 2);
      CanvasGraphics.fill(
          batch, MorphCoUI.STEEL, alpha, innerX + innerWidth - 2, innerY, 2, top - innerY);
    }
    if (over)
      CanvasGraphics.fill(
          batch, MorphCoUI.SIGNAL, alpha * .12f, innerX, innerY, innerWidth, top - innerY);
    // The edge of the mouth casts a shadow into it.
    CanvasGraphics.fill(batch, Color.BLACK, alpha * .5f, innerX, top - 6, innerWidth, 6);
    CanvasGraphics.fill(batch, Color.BLACK, alpha * .25f, innerX, top - 12, innerWidth, 6);
    MorphCoUI.hazard(batch, alpha, innerX, top + 1, innerWidth, 6);
  }

  // An open crate seen from above: steel walls around a dark floor with slats.
  private static void drawCrate(
      Batch batch, float alpha, float x, float y, float width, float height, boolean over) {
    MorphCoUI.plate(batch, MorphCoUI.CRATE, alpha, x, y, width, height);
    float innerX = x + BIN_PAD - 4;
    float innerY = y + BIN_PAD - 4;
    float innerWidth = width - 2 * BIN_PAD + 8;
    float innerHeight = height - BIN_HEADER - 2 * BIN_PAD + 8;
    Color floor = over ? MorphCoUI.STEEL_DARK : MorphCoUI.CRATE_DARK;
    CanvasGraphics.fill(batch, floor, alpha, innerX, innerY, innerWidth, innerHeight);
    for (float slat = innerX + 24; slat < innerX + innerWidth - 8; slat += 26)
      CanvasGraphics.fill(batch, MorphCoUI.OUTLINE, alpha * .6f, slat, innerY, 2, innerHeight);
    // The inside of the far wall.
    CanvasGraphics.fill(
        batch,
        MorphCoUI.CRATE.cpy().lerp(Color.BLACK, .3f),
        alpha,
        innerX,
        innerY + innerHeight - 8,
        innerWidth,
        8);
  }
}
