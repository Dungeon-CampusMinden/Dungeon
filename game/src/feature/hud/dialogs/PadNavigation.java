package feature.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import engine.input.GameInput;
import engine.input.InputDevice;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.interaction.InteractionDialogColors;
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
final class PadNavigation {
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
   * degrees of it. If the cursor rests on no control and the scroll pane under it can still scroll
   * in the direction, only the controls in view in that pane count. If it rests on no control and
   * none lies in the direction, it moves to the nearest control in any direction, unless the scroll
   * pane under it can still scroll there.
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
        controls.stream().filter(control -> hits(stage, control, pointer)).findFirst().orElse(null);
    if (current instanceof ScrollPane pane && scrolls(pane, dy)) return false;

    // Over text in a scroll pane, the pane keeps the D-pad until its end, apart from the controls
    // in view in it.
    ScrollPane pane = current == null ? paneAt(stage, pointer) : null;
    if (scrolls(pane, dy)) {
      controls.removeIf(control -> {
        Vector2 point = visiblePoint(stage, control);
        return scrollPane(control) != pane || point == null || !contains(pane, point);
      });
    }
    Actor best = nearest(stage, controls, current, pointer, dx, dy);
    if (best == null && current == null && !scrolls(pane, dy)) {
      best = nearest(stage, controls, null, pointer, 0, 0);
    }
    if (best == null) return false;

    scrollIntoView(best);
    Vector2 point = visiblePoint(stage, best);
    pointer(stage, point != null ? point : center(best));
    frame(dialog).target = best;
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
    return hit == null ? null : scrollPane(hit);
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

  private static boolean hits(Stage stage, Actor control, Vector2 stagePoint) {
    Actor hit = stage.hit(stagePoint.x, stagePoint.y, true);
    return hit != null && (hit == control || hit.isDescendantOf(control));
  }

  // The first point of a grid over the control that no other actor covers, the centre first; null
  // if it is hidden. A control scrolled out of its pane counts with its centre: the pane scrolls it
  // into view first.
  private static Vector2 visiblePoint(Stage stage, Actor control) {
    Vector2 center = center(control);
    ScrollPane pane = scrollPane(control);
    if (pane != null && !contains(pane, center)) return center;
    for (float[] spot : SPOTS) {
      Vector2 point =
          control.localToStageCoordinates(
              new Vector2(control.getWidth() * spot[0], control.getHeight() * spot[1]));
      if (hits(stage, control, point)) return point;
    }
    return null;
  }

  private static ScrollPane scrollPane(Actor actor) {
    for (Group parent = actor.getParent(); parent != null; parent = parent.getParent()) {
      if (parent instanceof ScrollPane pane) return pane;
    }
    return null;
  }

  private static void scrollIntoView(Actor control) {
    ScrollPane pane = scrollPane(control);
    if (pane == null) return;
    Vector2 corner = control.localToActorCoordinates(pane.getWidget(), new Vector2());
    // libGDX's scrollTo takes the top edge as y, despite its y-up coordinates; with the bottom edge
    // it misses by the control's height.
    pane.scrollTo(
        corner.x, corner.y + control.getHeight(), control.getWidth(), control.getHeight());
    pane.updateVisualScroll();
    pane.layout();
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
  private static final class Frame extends Actor {
    private Actor target;

    private Frame() {
      setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      if (target == null || target.getStage() == null || !isControl(target)) return;
      if (GameInput.device() != InputDevice.PAD) return;
      if (!hits(target.getStage(), target, pointerOnStage(target.getStage()))) return;
      // A control wider than its scroll pane shows only inside it, and so does its frame.
      Rectangle box = bounds(target);
      ScrollPane pane = scrollPane(target);
      if (pane != null) Intersector.intersectRectangles(bounds(target), bounds(pane), box);
      float x = getX() + box.x;
      float y = getY() + box.y;
      CanvasGraphics.outline(
          batch, Color.BLACK, parentAlpha, x - 5, y - 5, box.width + 10, box.height + 10, 2);
      CanvasGraphics.outline(
          batch,
          InteractionDialogColors.SELECTED,
          parentAlpha,
          x - 3,
          y - 3,
          box.width + 6,
          box.height + 6,
          3);
    }

    // The bounds of an actor in the dialog, which holds the frame.
    private Rectangle bounds(Actor actor) {
      Vector2 from = actor.localToActorCoordinates(getParent(), new Vector2());
      Vector2 to =
          actor.localToActorCoordinates(
              getParent(), new Vector2(actor.getWidth(), actor.getHeight()));
      return new Rectangle(
          Math.min(from.x, to.x),
          Math.min(from.y, to.y),
          Math.abs(to.x - from.x),
          Math.abs(to.y - from.y));
    }
  }
}
