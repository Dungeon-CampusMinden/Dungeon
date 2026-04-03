package contrib.hud.crafting;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Small backend-facing abstraction for the crafting dialog action bar.
 *
 * <p>This interface does not make the crafting dialog fully engine-neutral yet, because the
 * surrounding dialog still renders via libGDX {@link Batch}. However, it removes the direct
 * dependency on a concrete backend helper type from {@link CraftingGUI}.
 */
public interface CraftingActionBar {

  /**
   * Updates the absolute on-screen bounds of the action bar.
   *
   * @param x left x position of the parent dialog
   * @param y bottom y position of the parent dialog
   * @param width width of the parent dialog
   * @param height height of the parent dialog
   */
  void updateBounds(int x, int y, int width, int height);

  /**
   * Draws the action bar.
   *
   * @param batch libGDX batch used by the current dialog rendering path
   */
  void draw(Batch batch);
}
