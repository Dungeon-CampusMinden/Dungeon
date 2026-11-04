package engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import engine.utils.CursorUtil;
import engine.utils.Cursors;
import engine.utils.Vector2;
import java.util.EnumMap;
import java.util.Map;

/**
 * Lets the pad use the UI through pointer 0: {@link UiActions#CLICK} becomes touch, drag and cancel
 * events at the pointer, and movement becomes hover. These events go straight to the stage and
 * never back through the resolver. A drag keeps its receiver through the stage's touch focus.
 */
final class PadCursor {
  private static final Map<Cursors, Texture> textures = new EnumMap<>(Cursors.class);
  private static boolean touching;
  private static Vector2 lastPosition = Vector2.ZERO;

  private PadCursor() {}

  static void update(InputResolver resolver, Stage stage) {
    if (stage == null) return;
    Vector2 position = resolver.pointer(0);
    int x = (int) position.x();
    int y = (int) position.y();
    boolean moved = !position.equals(lastPosition);
    lastPosition = position;
    boolean pressed = resolver.pressed(UiActions.CLICK);

    if (pressed && !touching) {
      touching = true;
      stage.touchDown(x, y, 0, Input.Buttons.LEFT);
    } else if (!pressed && touching) {
      touching = false;
      if (resolver.released(UiActions.CLICK)) stage.touchUp(x, y, 0, Input.Buttons.LEFT);
      else stage.touchCancelled(x, y, 0, Input.Buttons.LEFT);
    } else if (touching && moved) {
      stage.touchDragged(x, y, 0);
    } else if (moved && resolver.device() == InputDevice.PAD) {
      stage.mouseMoved(x, y);
    }
  }

  /**
   * Draws the cursor at pointer 0 while the pad is the active device.
   *
   * @param batch batch in stage coordinates, not yet begun
   * @param stage the stage the cursor is drawn on
   */
  static void draw(Batch batch, Stage stage) {
    if (GameInput.device() != InputDevice.PAD) return;
    Cursors cursor = CursorUtil.getCurrentCursor();
    if (cursor == null) return;
    Texture texture =
        textures.computeIfAbsent(cursor, c -> new Texture(Gdx.files.internal(c.path())));
    Vector2 position = GameInput.pointer();
    com.badlogic.gdx.math.Vector2 stagePosition =
        stage.screenToStageCoordinates(
            new com.badlogic.gdx.math.Vector2(position.x(), position.y()));
    batch.setProjectionMatrix(stage.getCamera().combined);
    batch.begin();
    batch.draw(
        texture,
        stagePosition.x - cursor.hotspotX(),
        stagePosition.y - texture.getHeight() + cursor.hotspotY());
    batch.end();
  }
}
