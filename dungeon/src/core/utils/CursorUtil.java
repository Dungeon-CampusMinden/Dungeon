package core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;

/** Utility class for managing custom mouse cursors in LibGDX. */
public class CursorUtil {

  private static Cursors currentCursor;

  /**
   * Set the mouse cursor to the specified type.
   *
   * @param cursor the type of cursor to set
   */
  public static void setCursor(Cursors cursor) {
    Pixmap pixmap = new Pixmap(Gdx.files.internal(cursor.path()));
    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, cursor.hotspotX(), cursor.hotspotY()));
    pixmap.dispose();
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
            Cursors current = getCurrentCursor();
            Cursors target = Cursors.DEFAULT;

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

            if (current != target) {
              setCursor(target);
            }
            return false;
          }
        });
  }
}
