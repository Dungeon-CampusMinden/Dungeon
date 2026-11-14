package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.interaction.sorting.SortBoard;
import java.util.List;

/**
 * The engine's sort board in the look of the hall: chips are factory tags with a punched hole, bins
 * are steel crates with a name plate.
 */
final class MorphCoSortBoard extends SortBoard {

  MorphCoSortBoard(CanvasArea area, Runnable onChange) {
    super(area, onChange);
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
    MorphCoUI.plate(batch, MorphCoUI.STEEL, alpha, x, y, width, height);
    MorphCoUI.plate(
        batch,
        MorphCoUI.TAG,
        alpha,
        x + 12,
        y + height - BIN_HEADER + 2,
        width - 24,
        BIN_HEADER - 8);
    CanvasGraphics.fill(
        batch,
        over ? MorphCoUI.STEEL_DARK : MorphCoUI.INSET,
        alpha,
        x + BIN_PAD - 4,
        y + BIN_PAD - 4,
        width - 2 * BIN_PAD + 8,
        height - BIN_HEADER - 2 * BIN_PAD + 8);
    if (over || armed)
      CanvasGraphics.outline(
          batch, MorphCoUI.SIGNAL, alpha * (over ? 1 : .6f), x, y, width, height, over ? 4 : 2);
  }
}
