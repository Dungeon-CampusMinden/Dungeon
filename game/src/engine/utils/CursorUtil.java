package engine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
   * cursor instead of {@link Cursors#DEFAULT} when the pointer is over the world. The UI under the
   * pointer keeps priority when the world target changes.
   *
   * @param cursor the world cursor to use as the fallback
   */
  public static void setWorldCursor(Cursors cursor) {
    worldCursorOverride = cursor;
    refreshCursor();
  }

  /**
   * Clear the world-cursor override. The Stage listener will fall back to {@link Cursors#DEFAULT}
   * again. The UI under the pointer keeps its own cursor.
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
   * Initialize the cursor management system by adding an input listener to the specified stage.
   *
   * @param stage the stage to which the input listener will be added
   */
  public static void initListener(Stage stage) {
    resetCursor();
    stage.addAction(
        new Action() {
          private final Vector2 pointer = new Vector2();

          @Override
          public boolean act(float delta) {
            stage.screenToStageCoordinates(pointer.set(Gdx.input.getX(), Gdx.input.getY()));
            setCursor(cursorFor(stage.hit(pointer.x, pointer.y, true)));
            return false;
          }
        });
    stage.addListener(
        new InputListener() {
          @Override
          public boolean mouseMoved(InputEvent event, float x, float y) {
            setCursor(cursorFor(stage.hit(x, y, true)));
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
  public static Cursors cursorFor(Actor hit) {
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
