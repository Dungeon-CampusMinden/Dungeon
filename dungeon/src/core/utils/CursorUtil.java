package core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import java.util.EnumMap;
import java.util.Map;

/** Utility class for managing custom mouse cursors in LibGDX. */
public class CursorUtil {

  private static Cursors currentCursor;

  /** Cache of native cursor objects keyed by cursor type to avoid repeated allocation. */
  private static final Map<Cursors, Cursor> cursorCache = new EnumMap<>(Cursors.class);

  /**
   * Optional world-cursor override. When set, the Stage input listener uses this instead of {@link
   * Cursors#DEFAULT} as its fallback when no UI element requests a specific cursor. This prevents
   * the Stage listener from flickering back to DEFAULT every frame while the game wants a different
   * cursor (e.g., INTERACT).
   */
  private static Cursors worldCursorOverride = null;

  /**
   * Set the mouse cursor to the specified type. Native cursor objects are cached so that repeated
   * calls with the same type do not allocate new resources.
   *
   * @param cursor the type of cursor to set
   */
  public static void setCursor(Cursors cursor) {
    if (cursor == currentCursor) return;
    Cursor nativeCursor =
        cursorCache.computeIfAbsent(
            cursor,
            c -> {
              Pixmap pixmap = new Pixmap(Gdx.files.internal(c.path()));
              Cursor created = Gdx.graphics.newCursor(pixmap, c.hotspotX(), c.hotspotY());
              pixmap.dispose();
              return created;
            });
    Gdx.graphics.setCursor(nativeCursor);
    currentCursor = cursor;
  }

  /** Reset the mouse cursor to the default type. */
  public static void resetCursor() {
    setCursor(Cursors.DEFAULT);
  }

  /**
   * Get the currently active mouse cursor type.
   *
   * @return the currently active mouse cursor type
   */
  public static Cursors getCurrentCursor() {
    return currentCursor;
  }

  /**
   * Set a world-cursor override. While active, the Stage input listener will fall back to this
   * cursor instead of {@link Cursors#DEFAULT} when no UI element overrides the cursor. If the
   * current cursor is DEFAULT or the previous world override, it is immediately switched to the new
   * override.
   *
   * @param cursor the world cursor to use as the fallback
   */
  public static void setWorldCursor(Cursors cursor) {
    worldCursorOverride = cursor;
    // Apply immediately if no UI element is overriding the cursor
    if (currentCursor == Cursors.DEFAULT || currentCursor == cursor) {
      setCursor(cursor);
    }
  }

  /**
   * Clear the world-cursor override. The Stage listener will fall back to {@link Cursors#DEFAULT}
   * again. If the current cursor equals the old override it is reset to DEFAULT.
   */
  public static void clearWorldCursor() {
    Cursors old = worldCursorOverride;
    worldCursorOverride = null;
    if (currentCursor == old) {
      resetCursor();
    }
  }

  /**
   * Initialize the cursor management system by adding an input listener to the specified stage.
   *
   * @param stage the stage to which the input listener will be added
   */
  public static void initListener(Stage stage) {
    resetCursor();
    stage.addListener(
        new InputListener() {
          @Override
          public boolean mouseMoved(InputEvent event, float x, float y) {
            Actor hit = stage.hit(x, y, true);
            // Fall back to the world override (if any) instead of hard-coding DEFAULT
            Cursors fallback = worldCursorOverride != null ? worldCursorOverride : Cursors.DEFAULT;
            Cursors target = fallback;

            while (hit != null) {
              if (hit.getUserObject() instanceof Cursors c) {
                if (hit instanceof Disableable d && d.isDisabled()) {
                  target = Cursors.DISABLED;
                  break;
                }
                target = c;
                break;
              }
              hit = hit.getParent();
            }

            if (getCurrentCursor() != target) {
              setCursor(target);
            }
            return false;
          }
        });
  }
}
