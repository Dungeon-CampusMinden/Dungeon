package engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import engine.utils.CursorUtil;
import engine.utils.Cursors;
import engine.utils.Vector2;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Lets the pad use the UI through pointer 0: {@link UiActions#CLICK} becomes touch, drag and cancel
 * events at the pointer, and movement becomes hover. These events go straight to the stage and
 * never back through the resolver. A drag keeps its receiver through the stage's touch focus.
 *
 * <p>A press on something draggable (see {@link GameInput#keepPadPress()}) stays down after the
 * button comes up, and the next press drops. Leaving the UI or the pad cancels it.
 */
final class PadCursor {
  private static final Map<Cursors, Texture> textures = new EnumMap<>(Cursors.class);
  private static boolean touching;
  private static boolean keepPress;
  private static InputContext keptBy;
  private static Vector2 lastPosition = Vector2.ZERO;

  private PadCursor() {}

  static void reset() {
    touching = false;
    keepPress = false;
    keptBy = null;
  }

  static void keepPress() {
    keepPress = true;
  }

  static void update(InputResolver resolver, Stage stage) {
    if (stage == null) return;
    Vector2 position = resolver.pointer(0);
    int x = (int) position.x();
    int y = (int) position.y();
    boolean moved = !position.equals(lastPosition);
    lastPosition = position;
    boolean pressed = resolver.pressed(UiActions.CLICK);

    if (keptBy != null) {
      updateKept(resolver, stage, x, y, moved);
    } else if (resolver.justPressed(UiActions.CLICK) && !touching) {
      touching = true;
      keepPress = false;
      GameInput.clearTextFocusOutside(x, y);
      stage.touchDown(x, y, 0, Input.Buttons.LEFT);
    } else if (!pressed && touching) {
      InputContext owner = clickOwner(resolver);
      if (!resolver.justReleased(UiActions.CLICK)) {
        touching = false;
        stage.touchCancelled(x, y, 0, Input.Buttons.LEFT);
      } else if (keepPress && owner != null) {
        keptBy = owner;
      } else {
        touching = false;
        stage.touchUp(x, y, 0, Input.Buttons.LEFT);
      }
    } else if (touching && moved) {
      stage.touchDragged(x, y, 0);
    } else if (moved && resolver.device() == InputDevice.PAD) {
      stage.mouseMoved(x, y);
    }
  }

  private static void updateKept(InputResolver resolver, Stage stage, int x, int y, boolean moved) {
    if (clickOwner(resolver) != keptBy || resolver.device() != InputDevice.PAD) {
      reset();
      stage.touchCancelled(x, y, 0, Input.Buttons.LEFT);
    } else if (resolver.justPressed(UiActions.CLICK)) {
      reset();
      stage.touchUp(x, y, 0, Input.Buttons.LEFT);
    } else if (moved) {
      stage.touchDragged(x, y, 0);
    }
  }

  // The topmost context that takes the click, which is the UI the press belongs to.
  private static InputContext clickOwner(InputResolver resolver) {
    List<InputContext> contexts = resolver.contexts();
    for (int i = contexts.size() - 1; i >= 0; i--) {
      if (contexts.get(i).spec().actions().contains(UiActions.CLICK)) return contexts.get(i);
    }
    return null;
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
    batch.setColor(Color.WHITE);
    batch.begin();
    batch.draw(
        texture,
        stagePosition.x - cursor.hotspotX(),
        stagePosition.y - texture.getHeight() + cursor.hotspotY());
    batch.end();
  }
}
