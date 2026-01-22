package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;

public class CursorUtil {

  private static Cursors currentCursor;

  public static void setCursor(Cursors cursor){
    Pixmap pixmap = new Pixmap(Gdx.files.internal(cursor.path()));
    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, cursor.hotspotX(), cursor.hotspotY()));
    pixmap.dispose();
    currentCursor = cursor;
  }

  public static void resetCursor(){
    setCursor(Cursors.DEFAULT);
  }

  public static Cursors getCurrentCursor() {
    return currentCursor;
  }

  public static void initListener(Stage stage){
    resetCursor();
    stage.addListener(new InputListener() {
      @Override
      public boolean mouseMoved(InputEvent event, float x, float y) {
        Actor hit = stage.hit(x, y, true);
        Cursors current = getCurrentCursor();
        Cursors target = Cursors.DEFAULT;

        while (hit != null) {
          if (hit.getUserObject() instanceof Cursors c) {
            if(hit instanceof Disableable d && d.isDisabled()){
              target = Cursors.DISABLED;
              break;
            }
            target = c;
            break;
          }
          hit = hit.getParent();
        }

        if(current != target) {
          setCursor(target);
        }
        return false;
      }
    });
  }

}
