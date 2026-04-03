package contrib.hud.crafting;

import com.badlogic.gdx.graphics.g2d.Batch;
import contrib.crafting.CraftingDialogController;

/**
 * Backend-facing renderer abstraction for the body of the crafting dialog.
 *
 * <p>This interface is intentionally still Batch-based, because it only serves the remaining
 * libGDX dialog path. The LITIENGINE dialog keeps its own Graphics2D overlay implementation.
 */
public interface CraftingDialogBodyRenderer {

  /**
   * Draws the crafting dialog body.
   *
   * @param batch target batch
   * @param controller shared crafting controller
   * @param x dialog x
   * @param y dialog y
   * @param width dialog width
   * @param height dialog height
   */
  void draw(Batch batch, CraftingDialogController controller, int x, int y, int width, int height);
}
