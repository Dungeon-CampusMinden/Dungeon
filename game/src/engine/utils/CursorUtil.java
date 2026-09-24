package engine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import engine.Game;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** Utility class for managing custom mouse cursors in LibGDX. */
public class CursorUtil {

  /** A UI region can override child cursors during an operation such as drag-and-drop. */
  public interface CursorOverride {
    /**
     * @return an active operation's cursor, or empty to use the hovered actor's cursor
     */
    Optional<Cursors> cursorOverride();
  }

  private static Cursors currentCursor;

  /** Cache of native cursor objects keyed by cursor type to avoid repeated allocation. */
  private static final Map<Cursors, Cursor> cursorCache = new EnumMap<>(Cursors.class);

  /**
   * Optional world-cursor override. When set, it replaces {@link Cursors#DEFAULT} while the pointer
   * is over the world instead of a UI element, for example INTERACT above an interactable entity.
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
   * Set a world-cursor override. While active, this cursor replaces {@link Cursors#DEFAULT} when
   * the pointer is over the world. The UI under the pointer keeps priority.
   *
   * @param cursor the world cursor to use as the fallback
   */
  public static void setWorldCursor(Cursors cursor) {
    worldCursorOverride = cursor;
    refreshCursor();
  }

  /**
   * Clear the world-cursor override. The world falls back to {@link Cursors#DEFAULT} again; the UI
   * under the pointer keeps its own cursor.
   */
  public static void clearWorldCursor() {
    worldCursorOverride = null;
    refreshCursor();
  }

  private static void refreshCursor() {
    Actor hit =
        Game.stage()
            .map(
                stage -> {
                  Vector2 pointer =
                      stage.screenToStageCoordinates(
                          new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                  return stage.hit(pointer.x, pointer.y, true);
                })
            .orElse(null);
    setCursor(cursorFor(hit));
  }

  /**
   * Initialize cursor management for the game stage. The cursor is resolved every frame, so UI that
   * opens or closes under a resting pointer updates it as well.
   *
   * @param stage the game stage
   */
  public static void initListener(Stage stage) {
    resetCursor();
    stage.addAction(
        new Action() {
          @Override
          public boolean act(float delta) {
            refreshCursor();
            return false;
          }
        });
  }

  /**
   * Resolves the nearest cursor tag, with active UI operations taking priority over child controls.
   *
   * @param hit actor under the pointer, or null for the world
   * @return the cursor for the current UI and world state
   */
  private static Cursors cursorFor(Actor hit) {
    Cursors target = null;
    for (Actor actor = hit; actor != null; actor = actor.getParent()) {
      if (actor instanceof CursorOverride override) {
        var cursor = override.cursorOverride();
        if (cursor.isPresent()) return cursor.get();
      }
      if (target == null && actor.getUserObject() instanceof Cursors cursor)
        target = actor instanceof Disableable d && d.isDisabled() ? Cursors.DISABLED : cursor;
    }
    return target != null
        ? target
        : hit == null && worldCursorOverride != null ? worldCursorOverride : Cursors.DEFAULT;
  }
}
