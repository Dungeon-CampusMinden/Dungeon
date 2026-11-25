package feature.hud.dialogs;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import engine.input.FocusFrame;
import engine.input.GameInput;
import engine.input.InputDevice;
import feature.canvas.CanvasNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets the D-pad move the pad cursor from one control of a dialog to the next, so that dialogs made
 * for the mouse work with the pad: A then clicks, picks up and drops at the cursor as it does after
 * moving the cursor with the right stick. A frame marks the control under the cursor.
 *
 * <p>Controls are the innermost visible, touchable actors with an input listener, buttons for
 * example, and the movable nodes of a canvas, whose drags the canvas handles. Mere scenery that
 * must swallow presses takes a plain event listener instead of an input listener, so the D-pad
 * passes it by. A control partly covered by others is aimed at where it shows, one hidden entirely
 * is skipped; one scrolled out of its scroll pane is scrolled into view. A scroll pane under the
 * cursor keeps the D-pad for scrolling until its end.
 */
public final class PadNavigation {
  // Added to the score of controls outside the 45-degree cone, so they count only without others.
  private static final float OUT_OF_CONE = 1e6f;
  private static final float[][] SPOTS = {
    {.5f, .5f},
    {.5f, .25f},
    {.5f, .75f},
    {.25f, .5f},
    {.75f, .5f},
    {.25f, .25f},
    {.75f, .25f},
    {.25f, .75f},
    {.75f, .75f}
  };

  private PadNavigation() {}

  /**
   * Moves the pad cursor to the nearest control in a direction, preferring controls within 45
   * degrees of it. If the cursor rests on no control, the scroll pane under it lets only the
   * controls in view in it count until it can scroll no further in the direction, and with no
   * control in the direction then, the cursor moves to the nearest control in any direction.
   *
   * @param dialog the dialog's outermost actor
   * @param dx horizontal direction, -1, 0 or 1
   * @param dy vertical direction in stage coordinates (up is 1), -1, 0 or 1
   * @return true if the cursor moved; false also while the scroll pane under it can still scroll
   *     vertically in the direction
   */
  static boolean move(Group dialog, int dx, int dy) {
    Stage stage = dialog.getStage();
    if (stage == null) return false;
    Vector2 pointer = pointerOnStage(stage);
    List<Actor> controls = new ArrayList<>();
    collect(dialog, controls);
    Actor current =
        controls.stream()
            .filter(control -> FocusFrame.hits(stage, control, pointer))
            .findFirst()
            .orElse(null);
    if (current instanceof ScrollPane pane && scrolls(pane, dy)) return false;

    ScrollPane textPane = current == null ? paneAt(stage, pointer) : null;
    if (scrolls(textPane, dy)) {
      controls.removeIf(control -> {
        Vector2 point = visiblePoint(stage, control);
        return !control.isDescendantOf(textPane) || point == null || !contains(textPane, point);
      });
    }
    Actor best = nearest(stage, controls, current, pointer, dx, dy);
    if (best == null && current == null && !scrolls(textPane, dy)) {
      best = nearest(stage, controls, null, pointer, 0, 0);
    }
    if (best == null) return false;

    FocusFrame.scrollIntoView(best);
    Vector2 point = visiblePoint(stage, best);
    pointer(stage, point != null ? point : center(best));
    frame(dialog).target(best);
    return true;
  }

  // The best control in the direction, or with no direction the nearest one apart from one right
  // at the cursor, which would keep the cursor in place.
  private static Actor nearest(
      Stage stage, List<Actor> controls, Actor current, Vector2 pointer, int dx, int dy) {
    Actor best = null;
    float bestScore = Float.MAX_VALUE;
    for (Actor control : controls) {
      if (control == current) continue;
      Vector2 point = visiblePoint(stage, control);
      if (point == null) continue;
      Vector2 offset = point.sub(pointer);
      float along = offset.x * dx + offset.y * dy;
      float across = Math.abs(offset.x * dy - offset.y * dx);
      boolean directed = dx != 0 || dy != 0;
      // At least a pixel: a control in the same row lies a rounding error ahead.
      if (directed ? along < 1 : offset.len() < 1) continue;
      float score =
          directed ? along + 2 * across + (across <= along ? 0 : OUT_OF_CONE) : offset.len();
      if (score < bestScore) {
        best = control;
        bestScore = score;
      }
    }
    return best;
  }

  private static boolean scrolls(ScrollPane pane, int dy) {
    return pane != null
        && ((dy > 0 && pane.getScrollY() > 0) || (dy < 0 && pane.getScrollY() < pane.getMaxY()));
  }

  private static ScrollPane paneAt(Stage stage, Vector2 stagePoint) {
    Actor hit = stage.hit(stagePoint.x, stagePoint.y, true);
    if (hit instanceof ScrollPane pane) return pane;
    return hit == null ? null : FocusFrame.scrollPane(hit);
  }

  /**
   * Moves the pad cursor onto the centre of an actor, e.g. after the actor moved away under it.
   *
   * @param actor an actor on a stage
   */
  public static void point(Actor actor) {
    pointer(actor.getStage(), center(actor));
  }

  private static void pointer(Stage stage, Vector2 stagePoint) {
    Vector2 screen = stage.stageToScreenCoordinates(stagePoint);
    GameInput.resolver().pointer(0, screen.x, screen.y);
  }

  private static void collect(Actor actor, List<Actor> controls) {
    if (!actor.isVisible() || actor.getTouchable() == Touchable.disabled) return;
    int before = controls.size();
    if (actor instanceof Group group) {
      for (Actor child : group.getChildren()) collect(child, controls);
    }
    if (controls.size() == before
        && actor.getTouchable() == Touchable.enabled
        && isControl(actor)) {
      controls.add(actor);
    }
  }

  private static boolean isControl(Actor actor) {
    if (actor instanceof Disableable disableable && disableable.isDisabled()) return false;
    if (actor instanceof CanvasNode node && node.canvas() != null && node.movable()) return true;
    for (EventListener listener : actor.getListeners()) {
      if (listener instanceof InputListener) return true;
    }
    return false;
  }

  private static Vector2 pointerOnStage(Stage stage) {
    return stage.screenToStageCoordinates(
        new Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
  }

  private static boolean contains(Actor actor, Vector2 stagePoint) {
    Vector2 local = actor.stageToLocalCoordinates(stagePoint.cpy());
    return local.x >= 0
        && local.x < actor.getWidth()
        && local.y >= 0
        && local.y < actor.getHeight();
  }

  private static Vector2 center(Actor actor) {
    return actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2));
  }

  // The first point of a grid over the control that no other actor covers, the centre first; null
  // if it is hidden. A control scrolled out of its pane counts with its centre: the pane scrolls it
  // into view first.
  private static Vector2 visiblePoint(Stage stage, Actor control) {
    Vector2 center = center(control);
    ScrollPane pane = FocusFrame.scrollPane(control);
    if (pane != null && !contains(pane, center)) return center;
    for (float[] spot : SPOTS) {
      Vector2 point =
          control.localToStageCoordinates(
              new Vector2(control.getWidth() * spot[0], control.getHeight() * spot[1]));
      if (FocusFrame.hits(stage, control, point)) return point;
    }
    return null;
  }

  private static Frame frame(Group dialog) {
    Frame frame = null;
    for (Actor child : dialog.getChildren()) {
      if (child instanceof Frame existing) frame = existing;
    }
    if (frame == null) {
      frame = new Frame();
      dialog.addActor(frame);
    }
    frame.toFront();
    return frame;
  }

  /** The frame around the control the D-pad moved to, while the pad cursor stays on it. */
  private static final class Frame extends FocusFrame {
    @Override
    protected boolean shows(Stage stage, Actor target) {
      return isControl(target)
          && GameInput.device() == InputDevice.PAD
          && FocusFrame.hits(stage, target, pointerOnStage(stage));
    }
  }
}
